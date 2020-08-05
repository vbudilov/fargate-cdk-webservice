package com.budilov.cdk.web;

import software.amazon.awscdk.core.Construct;
import software.amazon.awscdk.core.Stack;
import software.amazon.awscdk.core.StackProps;
import software.amazon.awscdk.services.certificatemanager.Certificate;
import software.amazon.awscdk.services.certificatemanager.CertificateProps;

import java.io.IOException;
import java.util.List;

/**
 * Stack that creates IAM roles for ECS
 */
public class CertificateStack extends Stack {
    public static Certificate certificate;

    public CertificateStack(final Construct scope, final String id) throws IOException {
        this(scope, id, null);
    }

    public CertificateStack(final Construct scope, final String id, final StackProps props) throws IOException {
        super(scope, id, props);

        String domainName = "budilov.com";
        String alternativeDomains = "*.budilov.com";

        certificate = new Certificate(this, "BudilovCertificate", new CertificateProps.Builder()
                .domainName(domainName)
                .subjectAlternativeNames(List.of(alternativeDomains)).build());


    }

}
