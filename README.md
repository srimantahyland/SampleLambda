# SampleLambda

To package the jar run the following :
mvn clean package shade:shade

Ref :

https://www.baeldung.com/java-aws-lambda

Given a list of comma separated instance ids this lambda function
will stop the instance.

This can be used with AWS Eventbridge to stop instance daily 
at a certain time.