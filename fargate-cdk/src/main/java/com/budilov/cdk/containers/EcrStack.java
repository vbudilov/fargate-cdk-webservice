package com.budilov.cdk.containers;

import com.budilov.cdk.util.Properties;
import software.amazon.awscdk.core.Construct;
import software.amazon.awscdk.core.Stack;
import software.amazon.awscdk.core.StackProps;
import software.amazon.awscdk.services.ecr.LifecycleRule;
import software.amazon.awscdk.services.ecr.Repository;
import software.amazon.awscdk.services.ssm.ParameterTier;
import software.amazon.awscdk.services.ssm.StringParameter;

import java.io.IOException;
import java.util.List;

/**
 * Stack that creates IAM roles for EKS Pods
 */
public class EcrStack extends Stack {

    public static Repository webServiceECR;

    public EcrStack(final Construct scope, final String id) throws IOException {
        this(scope, id, null);
    }

    public EcrStack(final Construct scope, final String id, final StackProps props) throws IOException {
        super(scope, id, props);

        webServiceECR = Repository.Builder.create(this, Properties.ECR_WEB_SERVICE_NAME)
                .repositoryName(Properties.ECR_WEB_SERVICE_NAME)
                .lifecycleRules(List.of(LifecycleRule
                        .builder()
                        .maxImageCount(9999)
                        .build())
                )
                .build();


        StringParameter.Builder.create(this, "webServiceECR")
                .allowedPattern(".*")
                .description("webServiceECR")
                .parameterName("webServiceECR")
                .stringValue(webServiceECR.getRepositoryName())
                .tier(ParameterTier.STANDARD)
                .build();

    }

}
