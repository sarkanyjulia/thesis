package speakeridentification.modeltrainer;

import java.util.Scanner;

import org.slf4j.Logger;

public class PretrainRunner {

    private static final Logger log = org.slf4j.LoggerFactory.getLogger(PretrainRunner.class);

    private static int numEpochs = 1;
    private static int batchSize = 10;

    public static void main(String[] args) {

        Pretrainer pretrainer;
        StringBuilder sb = new StringBuilder();
        if (args.length == 2) {
            pretrainer = new Pretrainer(args[0], args[1], null);
            sb.append("\nPath to train data: ").append(args[0])
                .append("\nPath to save directory: ").append(args[1]);
        }
        else if (args.length == 3) {
            pretrainer = new Pretrainer(args[0], args[1], args[2]);
            sb.append("\nTrain data: ").append(args[0])
                .append("\nSave directory: ").append(args[1])
                .append("\nModel to load: ").append(args[2]);
        }
        else {
            log.info("Wrong number of command line arguments.");
            throw new RuntimeException("Wrong number of arguments.");
        }
        readParams();
        sb.append("\nNumber of epochs: ").append(numEpochs).append("\nBatch size: ").append(batchSize);
        log.info(sb.toString());
        pretrainer.train(numEpochs, batchSize);
    }

    private static void readParams() {
        Scanner scanner = new Scanner(System.in);
        System.out.println("Number of epochs: ");
        try {
            int input = scanner.nextInt();
            if (input < 1) throw new Exception();
            numEpochs = input;
        } catch (Exception e){
            System.out.println("Number of epochs must be a positive integer. Continuing with default value (1).");
        }
        System.out.println("Batch size: ");
        try {
            int input = scanner.nextInt();
            if (input < 1) throw new Exception();
            batchSize = input;
        } catch (Exception e){
            System.out.println("Batch size must be a positive integer. Continuing with default value (10).");
        }
        scanner.close();
    }
}
