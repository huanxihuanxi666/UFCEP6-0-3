


 Deployment order

1. Deploy every file in `bpmn/**/Forms/`.
2. Deploy the five files in `bpmn/**/BPMN/`.
3. Deploy `bpmn/Hospital_Master_Process.bpmn` after the child processes.
4. Copy `External_Workers/.env.example` to your local environment and set `ZEEBE_ADDRESS` if the gateway is not local.
5. In `External_Workers`, run `mvn test`, then `mvn package`.
6. Run `java -jar target/hospital-external-workers-1.0.0.jar` and start `Process_Hospital_Master` from Camunda Operate.

 External worker job types

- `validate-referral-reference`
- `search-appointment-slot`
- `send-appointment-letter`
- `check-treatment-resource`
- `process-payment`
- `process-refund`
- `send-clinic-letter`

 Test evidence

`External_Workers/src/test/java/org/hospital/workers/WorkerLogicSmokeTest.java` provides worker component tests. Runtime screenshots are stored in `evidence/`. The complete acceptance test plan, test results and design justification are in `Submission_Compliance_Pack.docx`.
