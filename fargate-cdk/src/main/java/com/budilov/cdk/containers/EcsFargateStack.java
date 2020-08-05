package com.budilov.cdk.containers;


import com.budilov.cdk.util.Properties;
import com.budilov.cdk.web.CertificateStack;
import software.amazon.awscdk.core.Construct;
import software.amazon.awscdk.core.Stack;
import software.amazon.awscdk.core.StackProps;
import software.amazon.awscdk.services.ec2.Port;
import software.amazon.awscdk.services.ec2.Vpc;
import software.amazon.awscdk.services.ecs.Protocol;
import software.amazon.awscdk.services.ecs.*;
import software.amazon.awscdk.services.elasticloadbalancingv2.HealthCheck;
import software.amazon.awscdk.services.elasticloadbalancingv2.*;
import software.amazon.awscdk.services.iam.CompositePrincipal;
import software.amazon.awscdk.services.iam.ManagedPolicy;
import software.amazon.awscdk.services.iam.Role;
import software.amazon.awscdk.services.iam.ServicePrincipal;
import software.amazon.awscdk.services.logs.RetentionDays;

import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * Stack that creates IAM roles for EKS Pods
 */
public class EcsFargateStack extends Stack {

    public EcsFargateStack(final Construct scope, final String id) throws IOException {
        this(scope, id, null);
    }

    public EcsFargateStack(final Construct scope, final String id, final StackProps props) throws IOException {
        super(scope, id, props);
        final Number cpu = 1024;
        final Number memory = 2048;
        final Number containerPort = 8080;
        final Number listenPort = 443;
        final Number desiredInstanceCount = 1;

        Vpc vpc = Vpc.Builder.create(this, "myVPC")
                .build();

        // ALB
        ApplicationLoadBalancer alb = ApplicationLoadBalancer.Builder.create(this, "myALB")
                .vpc(vpc)
                .internetFacing(Boolean.TRUE)
                .loadBalancerName("my")
                .build();

        // ECS Cluster
        Cluster cluster = Cluster.Builder.create(this, "myECSCluster")
                .vpc(vpc)
                .clusterName("myECSCluster")
                .build();

        // Ingest Gateway Configuration
        // Let's define the task
        FargateTaskDefinition ccTaskDefinition = new FargateTaskDefinition(this, "mySearchServiceTask",
                FargateTaskDefinitionProps.builder()
                        .cpu(cpu)
                        .memoryLimitMiB(memory)
                        .taskRole(EcsIamStack.webServiceTaskIamRole)
                        .build());

        // Now the container
        ContainerDefinition ccContainerDefinition = ccTaskDefinition.addContainer("mySearchService", ContainerDefinitionOptions.builder()
                .image(ContainerImage.fromEcrRepository(EcrStack.webServiceECR))
                .environment(Map.of("MY_SERVICE_PORT", containerPort.toString()))
                .memoryLimitMiB(memory)
                .cpu(cpu)
                .logging(LogDriver.awsLogs(AwsLogDriverProps.builder()
                        .streamPrefix("mySearchService")
                        .logRetention(RetentionDays.ONE_DAY)
                        .build()))
                .build());
        ccContainerDefinition.addPortMappings(PortMapping.builder()
                .containerPort(containerPort)
                .protocol(Protocol.TCP)
                .build());

        // Create the Data Ingest Fargate Service
        FargateService ccFargateService = new FargateService(this, "myFargateService", FargateServiceProps.builder()
                .cluster(cluster)
                .taskDefinition(ccTaskDefinition)
                .desiredCount(desiredInstanceCount)
                .serviceName("myFargateService")
                .build());

        // Allow connections to the fargate service from the ALB TO the listed ports
        ccFargateService.getConnections().allowFrom(alb, Port.tcp(80));
        ccFargateService.getConnections().allowFrom(alb, Port.tcp(443));

        // Allow connections to the fargate service from the ALB FROM the listed ports
        alb.getConnections().allowTo(ccFargateService, Port.tcp(containerPort));

        ApplicationTargetGroup ccTargetGroup = new ApplicationTargetGroup(this, "myTargetGroup",
                ApplicationTargetGroupProps.builder()
                        .targets(List.of(ccFargateService))
                        .targetType(TargetType.IP)
                        .port(containerPort)
                        .protocol(ApplicationProtocol.HTTP)
                        .vpc(vpc)
                        .healthCheck(HealthCheck.builder()
                                .enabled(true)
                                .path("/ping")
                                .build())
                        .build());
        ListenerCertificate certificate = ListenerCertificate.fromCertificateManager(CertificateStack.certificate);

        ApplicationListener listener = alb.addListener("myAlbListener",
                BaseApplicationListenerProps.builder()
                        .protocol(ApplicationProtocol.HTTPS)
                        .port(listenPort)
                        .certificates(List.of(certificate))
                        .defaultTargetGroups(List.of(ccTargetGroup))
                        .build());

    }

    /**
     * Stack that creates IAM roles for tasks
     */
    public static class EcsIamStack extends Stack {

        static Role webServiceTaskIamRole;

        public EcsIamStack(final Construct scope, final String id) throws IOException {
            this(scope, id, null);
        }

        public EcsIamStack(final Construct scope, final String id, final StackProps props) throws IOException {
            super(scope, id, props);

            // Query service Role
            webServiceTaskIamRole = Role.Builder.create(this, Properties.MICRO_SERVICE_POD_ROLE)
                    .assumedBy(
                            new CompositePrincipal(
                                    ServicePrincipal.Builder.create("eks").build(),
                                    ServicePrincipal.Builder.create("ecs").build(),
                                    ServicePrincipal.Builder.create("ecs-tasks").build()
                            )
                    )
                    .managedPolicies(List.of(
                            ManagedPolicy.fromAwsManagedPolicyName("AmazonSSMFullAccess"),
                            ManagedPolicy.fromAwsManagedPolicyName("AmazonESFullAccess")))
                    .roleName(Properties.MICRO_SERVICE_POD_ROLE)
                    .build();

        }

    }
}
