package com.budilov.cdk;

import com.budilov.cdk.containers.EcrStack;
import com.budilov.cdk.containers.EcsFargateStack;
import com.budilov.cdk.web.CertificateStack;
import software.amazon.awscdk.core.App;

public class CdkApp {


    public static void main(final String[] args) throws Exception {
        App app = new App();

        // SPA
        CertificateStack certificateStack = new CertificateStack(app, "MyACMCertificate");

        // Containers
        EcsFargateStack.EcsIamStack ecsIamStack = new EcsFargateStack.EcsIamStack(app, "MyEcsIamStack");
        EcrStack ecrStack = new EcrStack(app, "MyEcrStack");
        EcsFargateStack ecsFargateStack = new EcsFargateStack(app, "MyFargate");
        ecsFargateStack.addDependency(certificateStack);
        ecsFargateStack.addDependency(ecsIamStack);
        ecsFargateStack.addDependency(ecrStack);

        app.synth();
    }
}
