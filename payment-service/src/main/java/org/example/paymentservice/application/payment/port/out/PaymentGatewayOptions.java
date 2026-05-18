package org.example.paymentservice.application.payment.port.out;

public record PaymentGatewayOptions(
        String customerIp,
        boolean require3DSecure,
        boolean isSandboxEnvironment
) {
    public static PaymentGatewayOptions standard(String ip) {
        return new PaymentGatewayOptions(ip, true, false);
    }
}