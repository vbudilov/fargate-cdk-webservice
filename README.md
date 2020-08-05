Sample Fargate + CDK + Web Service Project
=====

### Author: Vladimir Budilov
* [YouTube](https://www.youtube.com/channel/UCBl-ENwdTlUsLY05yGgXyxw)
* [LinkedIn](https://www.linkedin.com/in/vbudilov/)
* [Medium](https://medium.com/@budilov)

### What is it? 
This project (or set of projects) demonstrates the use of AWS CDK to provision an Amazon Fargate service with all of the peripheral services that are required (Application Load Balancer, ECR, SSM). 

### Deployment

The first step is to customize the code to your requirements...for the most part it involves changing the following lines of code in the CertificateStack class:

```
        String domainName = "budilov.com";
        String alternativeDomains = "*.budilov.com";
```

You might also consider changing the following values to your liking:

```
    public EcsFargateStack(final Construct scope, final String id, final StackProps props) throws IOException {
        super(scope, id, props);
        final Number cpu = 1024;
        final Number memory = 2048;
        final Number containerPort = 8080;
        final Number listenPort = 443;
        final Number desiredInstanceCount = 1;

```


Now you can start the build/deployment process:

```
cd fargate-cdk/
mvn package && cdk deploy "MyFargate"

```

Once you're at the ECR stage you will need to open up the console and follow the steps of uploading your first ECR image otherwise the provisioning process will stall (and eventually fail). The sample web service is under the web-service/ folder. 

### Calling the service

Look up the alb fqdn and paste it in the browser:
```
https://<fqdn>/ping
```

You should see a reply of 'pong'. 

Now let's look up the name of the ECR repo:

```
https://<fqdn>/meta/ecr
```

