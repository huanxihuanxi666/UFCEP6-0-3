 Camunda deployment configuration

Target platform: Camunda 8 self-managed or local Zeebe gateway.

1. Import and deploy all editable `.form` files before their BPMN process.
2. Deploy each child BPMN with the process IDs `Process_Referral`, `Process_Appointment`, `Process_Treatment`, `Process_Finance`, and `Process_EnquiryLetter`.
3. Deploy `Hospital_Master_Process.bpmn` last. Its Call Activities bind to the latest child-process deployment.
4. Set `ZEEBE_ADDRESS` from `External_Workers/.env.example` when using a non-default gateway.
5. From `External_Workers`, run `mvn test`, `mvn package`, then start the shaded JAR.

The worker application uses plaintext local Zeebe connectivity. For a managed Camunda cluster, replace the client construction with the approved cluster authentication configuration before deployment.
