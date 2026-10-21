package org.hospital.workers;

import io.camunda.zeebe.client.ZeebeClient;
import java.nio.file.Path;

/** Deploys the current operational BPMNs to a local Zeebe gateway as an integration smoke test. */
public final class DeploymentSmokeTest {
    private DeploymentSmokeTest() {}

    public static void main(String[] args) {
        Path root = Path.of(args.length == 0
                ? ".."
                : args[0]).toAbsolutePath().normalize();
        Path models = root.resolve("03_Operational_BPMN");
        Path[] resources = {
                models.resolve("23084366_DENG_JIN_WANG_Referral/Referral_Consultant_Review.bpmn"),
                models.resolve("23084364_WU_JIA_WEI_Appointment/Appointment_Patient_Communication.bpmn"),
                models.resolve("23084362_WU_TONG_YU_Finance/Funding_Payment_Refund.bpmn"),
                models.resolve("23084368_LIN_JIA_QIANG_Treatment/Treatment_Chemotherapy.bpmn"),
                models.resolve("24000813_XIAO_YU_YING_Enquiry_ClinicLetter/Enquiry_ClinicLetter_Monitoring.bpmn"),
                models.resolve("00_Integration/Hospital_Master_Process.bpmn")
        };

        try (ZeebeClient client = ZeebeClient.newClientBuilder()
                .gatewayAddress(System.getenv().getOrDefault("ZEEBE_ADDRESS", "127.0.0.1:26500"))
                .usePlaintext()
                .build()) {
            for (Path resource : resources) {
                if (!resource.toFile().isFile()) {
                    throw new IllegalStateException("BPMN missing: " + resource);
                }
            }
            var response = client.newDeployResourceCommand()
                    .addResourceFile(resources[0].toString())
                    .addResourceFile(resources[1].toString())
                    .addResourceFile(resources[2].toString())
                    .addResourceFile(resources[3].toString())
                    .addResourceFile(resources[4].toString())
                    .addResourceFile(resources[5].toString())
                    .send().join();
            System.out.println("DEPLOYMENT_PASSED processes=" + response.getProcesses().size());
            response.getProcesses().forEach(process ->
                    System.out.println(process.getBpmnProcessId() + " v" + process.getVersion()));
        }
    }
}
