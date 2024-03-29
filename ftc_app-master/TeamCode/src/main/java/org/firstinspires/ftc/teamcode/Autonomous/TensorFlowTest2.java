package org.firstinspires.ftc.teamcode.Autonomous;

import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.Disabled;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.util.ReadWriteFile;

import org.firstinspires.ftc.robotcore.external.ClassFactory;
import org.firstinspires.ftc.robotcore.external.hardware.camera.WebcamName;
import org.firstinspires.ftc.robotcore.external.navigation.VuforiaLocalizer;
import org.firstinspires.ftc.robotcore.external.tfod.Recognition;
import org.firstinspires.ftc.robotcore.external.tfod.TFObjectDetector;
import org.firstinspires.ftc.robotcore.internal.system.AppUtil;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Scanner;


/**
 * This 2018-2019 OpMode illustrates the basics of using the TensorFlow Object Detection API to
 * determine the position of the gold and silver minerals.
 * <p>
 * Use Android Studio to Copy this Class, and Paste it into your team's code folder with a new name.
 * Remove or comment out the @Disabled line to add this opmode to the Driver Station OpMode list.
 * <p>
 * IMPORTANT: In order to use this OpMode, you need to obtain your own Vuforia license key as
 * is explained below.
 */
@Autonomous(name = "123MATTRUNTHISAUTONOMOUS")
@Disabled
public class TensorFlowTest2 extends LinearOpMode {
    private static final String TFOD_MODEL_ASSET = "RoverRuckus.tflite";
    private static final String LABEL_GOLD_MINERAL = "Gold Mineral";
    private static final String LABEL_SILVER_MINERAL = "Silver Mineral";

    /*
     * IMPORTANT: You need to obtain your own license key to use Vuforia. The string below with which
     * 'parameters.vuforiaLicenseKey' is initialized is for illustration only, and will not function.
     * A Vuforia 'Development' license key, can be obtained free of charge from the Vuforia developer
     * web site at https://developer.vuforia.com/license-manager.
     *
     * Vuforia license keys are always 380 characters long, and look as if they contain mostly
     * random data. As an example, here is a example of a fragment of a valid key:
     *      ... yIgIzTqZ4mWjk9wd3cZO9T1axEqzuhxoGlfOOI2dRzKS4T0hQ8kT ...
     * Once you've obtained a license key, copy the string from the Vuforia web site
     * and paste it in to your code on the next line, between the double quotes.
     */
    private static final String VUFORIA_KEY = "AfsLjeX/////AAABmUQCh0kvTE6ghhE9k6hRvhKDXeYFiILf2hzZdxqve5WufF/kXsVxFfdGWx4cv8N8R9XmndWbAIm3zTSNY6wS95DKDN89ZMaY9+ICrg9Yk5IhwKQJTYRL6hybkYAGiEsQVlgCoG9/CtDExYIo0ztEE4AITeq6OC9qejJcGZHNk3L+tke4VkKWHv2CSpamz77A2ul34WjTsuIjNrznEFS7UQLQCY/EKCTGuQnbrQn8P3xNSUauF4EzfX0npPRT1LE9KJEBsuYaZUH7erzUGxKS4uOD7G3DUSQv+V0WRaXiWYNWP5SvacaCuGsaA7rZeLp/AIYjPNY7eKUp37BOYYK89Vat6pt1fQ9D4A1g5YYEDK2mv";

    /**
     * {@link #vuforia} is the variable we will use to store our instance of the Vuforia
     * localization engine.
     */
    private VuforiaLocalizer vuforia;
    private VuforiaLocalizer vuforiaCam;
    /**
     * {@link #tfod} is the variable we will use to store our instance of the Tensor Flow Object
     * Detection engine.
     */
    private TFObjectDetector tfod;

    private String[] recentResults = new String[10];

    @Override
    public void runOpMode() throws InterruptedException {
        // The TFObjectDetector uses the camera frames from the VuforiaLocalizer, so we create that
        // first.
        initVuforia();

        int goldMineralX = -1;
        String target = "Unknown";
        String filename = "AutonomousOptions.txt";
        File file = AppUtil.getInstance().getSettingsFile(filename);
        String fileText = ReadWriteFile.readFile(file);
        Scanner reader = new Scanner(fileText);
        double delay = reader.nextDouble();

        if (ClassFactory.getInstance().canCreateTFObjectDetector()) {
            initTfod();
        } else {
            telemetry.addData("Sorry!", "This device is not compatible with TFOD");
        }

        waitForStart();


        /** Wait for the game to begin */
        //telemetry.addData(">", "Press Play to start tracking");
        telemetry.update();


        if (!isStarted            /** Activate Tensor Flow Object Detection. */
                ()) {
            telemetry.addData("Delay", delay);
            if (tfod != null) {
                tfod.activate();
            }

            while (!isStarted()) {
                if (tfod != null) {
                    // getUpdatedRecognitions() will return null if no new information is available since
                    // the last time that call was made.
                    sleep(100);
                    List<Recognition> updatedRecognitions = tfod.getUpdatedRecognitions();
                    if (updatedRecognitions != null) {
                        int goldCount = 0;
                        int silverCount = 0;
                        for (Recognition recognition : updatedRecognitions) {
                            if (recognition.getLabel().equals(LABEL_GOLD_MINERAL)) {
                                goldCount++;
                                telemetry.addData("Gold " + goldCount + " Coordinates: ", (recognition.getLeft() + recognition.getRight()) / 2.0 + " " + (recognition.getTop() + recognition.getBottom()) / 2.0);
                            } else {
                                silverCount++;
                                telemetry.addData("Silver " + silverCount + " Coordinates: ", (recognition.getLeft() + recognition.getRight()) / 2.0 + " " + (recognition.getTop() + recognition.getBottom()) / 2.0);
                            }
                        }

                        ArrayList<Integer> removedInts = new ArrayList<>();
                        for (int i = 0; i < updatedRecognitions.size(); i++){
                            Recognition rec = updatedRecognitions.get(i);
                            for (int j = 0; j < updatedRecognitions.size(); j++){
                                Recognition rec2 = updatedRecognitions.get(j);
                                if (Math.abs((rec.getLeft() + rec.getRight())/2 - (rec2.getLeft() + rec.getRight())/2) < 150){
                                    if (rec.getConfidence() < rec2.getConfidence()){
                                        if (!removedInts.contains(i)){
                                            removedInts.add(i);
                                        }
                                    }
                                    if (rec.getConfidence() > rec2.getConfidence()){
                                        if (!removedInts.contains(j)){
                                            removedInts.add(j);
                                        }
                                    }
                                }
                            }
                        }
                        Collections.sort(removedInts, Collections.reverseOrder());
                        for (int i : removedInts){
                            updatedRecognitions.remove(i);
                        }

                        if (updatedRecognitions.size() == 3) {
                            goldMineralX = -1;
                            int silverMineral1X = -1;
                            int silverMineral2X = -1;
                            telemetry.addData("# Object Detected", updatedRecognitions.size());
                            for (Recognition recognition : updatedRecognitions) {
                                if (recognition.getLabel().equals(LABEL_GOLD_MINERAL)) {
                                    goldMineralX = (int) recognition.getLeft();
                                } else if (silverMineral1X == -1) {
                                    silverMineral1X = (int) recognition.getLeft();
                                } else {
                                    silverMineral2X = (int) recognition.getLeft();
                                }
                            }
                            if (goldMineralX != -1 && silverMineral1X != -1 && silverMineral2X != -1) {
                                if (goldMineralX < silverMineral1X && goldMineralX < silverMineral2X) {
                                    telemetry.addData("Gold Mineral Position", "Left");
                                    shiftArrayDown(recentResults, "L");
                                } else if (goldMineralX > silverMineral1X && goldMineralX > silverMineral2X) {
                                    telemetry.addData("Gold Mineral Position", "Right");
                                    shiftArrayDown(recentResults, "R");
                                } else {
                                    telemetry.addData("Gold Mineral Position", "Middle");
                                    shiftArrayDown(recentResults, "M");
                                }
                            }
                        }
                        //telemetry.update();

                        if (updatedRecognitions.size() == 2) {
                            goldMineralX = -1;
                            int silverMineral1X = -1;
                            int silverMineral2X = -1;
                            telemetry.addData("# Object Detected", updatedRecognitions.size());
                            for (Recognition recognition : updatedRecognitions) {
                                if (recognition.getLabel().equals(LABEL_GOLD_MINERAL)) {
                                    goldMineralX = (int) recognition.getLeft();
                                } else if (silverMineral1X == -1) {
                                    silverMineral1X = (int) recognition.getLeft();
                                } else {
                                    silverMineral2X = (int) recognition.getLeft();
                                }
                            }
                            if (goldMineralX == -1) {
                                telemetry.addData("Gold Mineral Position", "Right");
                                target = "Right";
                                shiftArrayDown(recentResults, "R");
                            }
                            else if (goldMineralX < silverMineral1X) {
                                telemetry.addData("Gold Mineral Position", "Left");
                                target = "Left";
                                shiftArrayDown(recentResults, "L");
                            } else {
                                telemetry.addData("Gold Mineral Position", "Middle");
                                target = "Middle";
                                shiftArrayDown(recentResults, "M");
                            }
                        }
                        if (updatedRecognitions.size() == 1){
                            goldMineralX = -1;
                            int silverMineral1X = -1;
                            telemetry.addData("# Object Detected", updatedRecognitions.size());
                            for (Recognition recognition : updatedRecognitions) {
                                if (recognition.getLabel().equals(LABEL_GOLD_MINERAL)) {
                                    goldMineralX = (int) (recognition.getLeft() + recognition.getRight() / 2);
                                }
                            }
                            if (goldMineralX < 500 && goldMineralX != -1){
                                telemetry.addData("Gold Mineral Position", "Left");
                                target = "Left";
                                shiftArrayDown(recentResults, "L");
                            }
                            if (goldMineralX > 500 && goldMineralX != -1){
                                telemetry.addData("Gold Mineral Position", "Middle");
                                target = "Middle";
                                shiftArrayDown(recentResults, "M");
                            }
                            if (goldMineralX == -1){
                                telemetry.addData("Gold Mineral Position", "Right (Sketch)");
                                target = "Right";
                                shiftArrayDown(recentResults, "R");
                            }
                        }
                    }

                }
                telemetry.addData("Recent results", Arrays.toString(recentResults));
                target = getTarget(recentResults);
                telemetry.addData("Gold X-Position", goldMineralX);
                telemetry.addData("Target", target);
                telemetry.update();
            }
        }
        if (tfod != null) {
            tfod.shutdown();
        }

    }

    public String[] shiftArrayDown(String[] array, String insertEnd) throws InterruptedException{
        for (int i = 0; i < array.length - 1; i++){
            array[i] = array[i + 1];
        }
        array[array.length - 1] = insertEnd;
        return array;
    }

    public String getTarget(String[] scores){
        int[] targetScores = {0, 0, 0};
        for (int i = 0; i < 10; i++){
            if (scores[i] != null) {
                if (scores[i].equals("L")) {
                    targetScores[0] += i;
                }
                if (scores[i].equals("M")) {
                    targetScores[1] += i;
                }
                if (scores[i].equals("R")) {
                    targetScores[2] += i;
                }
            }
        }
        if (targetScores[0] > targetScores[1] && targetScores[0] > targetScores[2]){
            return "Left";
        }
        else if (targetScores[1] > targetScores[2] && targetScores[1] > targetScores[0]){
            return "Middle";
        }
        return "Right";
    }

    /**
     * Initialize the Vuforia localization engine.
     */
    private void initVuforia() {
        /*
         * Configure Vuforia by creating a Parameter object, and passing it to the Vuforia engine.
         */
        VuforiaLocalizer.Parameters parameters = new VuforiaLocalizer.Parameters();

        parameters.vuforiaLicenseKey = VUFORIA_KEY;
        parameters.cameraName = hardwareMap.get(WebcamName.class, "Webcam 1");

        //  Instantiate the Vuforia engine
        vuforia = ClassFactory.getInstance().createVuforia(parameters);


        // Loading trackables is not necessary for the Tensor Flow Object Detection engine.
    }

    /**
     * Initialize the Tensor Flow Object Detection engine.
     */
    private void initTfod() {
        int tfodMonitorViewId = hardwareMap.appContext.getResources().getIdentifier(
                "tfodMonitorViewId", "id", hardwareMap.appContext.getPackageName());
        TFObjectDetector.Parameters tfodParameters = new TFObjectDetector.Parameters(tfodMonitorViewId);
        tfod = ClassFactory.getInstance().createTFObjectDetector(tfodParameters, vuforia);
        tfod.loadModelFromAsset(TFOD_MODEL_ASSET, LABEL_GOLD_MINERAL, LABEL_SILVER_MINERAL);

    }

    public void initVufPhoneCamera() {
        /*
         * Configure Vuforia by creating a Parameter object, and passing it to the Vuforia engine.
         */
        VuforiaLocalizer.Parameters parameters = new VuforiaLocalizer.Parameters();

        parameters.vuforiaLicenseKey = VUFORIA_KEY;
        parameters.cameraDirection = VuforiaLocalizer.CameraDirection.BACK;

        //  Instantiate the Vuforia engine
        vuforiaCam = ClassFactory.getInstance().createVuforia(parameters);



        // Loading trackables is not necessary for the Tensor Flow Object Detection engine.
    }

    public void initTfodPhoneCamera() {
        tfod.deactivate();
        tfod.shutdown();
        sleep(2000);
        int tfodMonitorViewId = hardwareMap.appContext.getResources().getIdentifier(
                "tfodMonitorViewId", "id", hardwareMap.appContext.getPackageName());
        TFObjectDetector.Parameters tfodParameters = new TFObjectDetector.Parameters(tfodMonitorViewId);
        tfod = ClassFactory.getInstance().createTFObjectDetector(tfodParameters, vuforia);
        tfod.loadModelFromAsset(TFOD_MODEL_ASSET, LABEL_GOLD_MINERAL, LABEL_SILVER_MINERAL);
    }
}