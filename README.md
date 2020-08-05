Sample Fargate + CDK + Web Service Project
=====

### Author: Vladimir Budilov
* [YouTube](https://www.youtube.com/channel/UCBl-ENwdTlUsLY05yGgXyxw)
* [LinkedIn](https://www.linkedin.com/in/vbudilov/)
* [Medium](https://medium.com/@budilov)

### What is it? 
This project (or set of projects) demonstrates the use of AWS CDK to provision an Amazon Fargate service with all of the peripheral services that are required (Application Load Balancer, ECR, SSM). 

### Deployment

cd fargate-cdk/
mvn package && cdk deploy "MyFargate"

Once you're at the ECR stage you will need to open up the console and follow the steps of uploading your first ECR image otherwise the provisioning process will stall (and eventually fail). The sample web service is under the web-service/ folder. 

### Calling the service

Look up the alb fqdn and paste it in the browser:
https://<fqdn>/ping

You should see a reply of 'pong'. 

Now let's look up the name of the ECR repo:
https://<fqdn>/meta/ecr

