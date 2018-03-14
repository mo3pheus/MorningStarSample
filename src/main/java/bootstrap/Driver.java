package bootstrap;

import egen.solutions.ennate.egen.solutions.sml.driver.SanketML;
import ennate.egen.solutions.sml.domain.ClassificationEngine;
import ennate.egen.solutions.sml.domain.ClusteringEngine;
import ennate.egen.solutions.sml.domain.Data;
import org.apache.log4j.EnhancedPatternLayout;
import org.apache.log4j.FileAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Priority;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Map;
import java.util.Properties;

public class Driver {
    public static final String SEPARATOR =
            "==============================================================";

    public static Properties projectProperties = new Properties();
    public static Logger     logger            = LoggerFactory.getLogger(Driver.class);
    public static String     inputFileName     = null;

    public static void main(String[] args) {
        try {
            String logFilePath = configureLogging(Boolean.parseBoolean(args[0]));
            logger.info(SEPARATOR);
            projectProperties = getProjectProperties(args[1]);

            System.out.println("Welcome to " + projectProperties.getProperty("project.name"));
            System.out.println(" Project properties are loaded. Log file generated for this run = " + logFilePath);

            inputFileName = projectProperties.getProperty("input.file.path");
            logger.info(SEPARATOR);

            int    numFields       = Integer.parseInt(projectProperties.getProperty("input.file.numColumns"));
            String delimiter       = projectProperties.getProperty("input.file.delimiter");
            int    trainingPercent = Integer.parseInt(projectProperties.getProperty("training.set.percentage"));

            SanketML mlProblem = new SanketML();
            try {
                mlProblem.loadData(inputFileName, delimiter, numFields);
            } catch (IOException e) {
                e.printStackTrace();
            }
            mlProblem.populateTrainTestSets(trainingPercent);

            System.out.println(" Number of training samples = " + mlProblem.getTrainingData().size());
            System.out.println(" Number of testing samples = " + mlProblem.getTestingData().size());

            ClassificationEngine classificationEngine = new ClassificationEngine();
            int                  numClusters          = Integer.parseInt(projectProperties.getProperty("kmeans.cluster.size"));
            classificationEngine.buildModels(mlProblem.getTrainingData(), numFields);

            ClassificationEngine.setDebugMode(true);
            mlProblem.setClassificationEngine(classificationEngine);
            System.out.println("Accuracy Percentage = " + mlProblem.getAccuracy() + " % ");

            System.out.println("\n\n\n ############################  CLUSTERING  " +
                                       "###################################\n\n ");
            ClusteringEngine clusterer   = new ClusteringEngine();
            try {
                Map<Data, ClusteringEngine.ClusteredPoints> result = clusterer.clusterData(mlProblem.getTrainingData
                        (), numClusters);
                mlProblem.setClusterer(clusterer);

                for (Data centroid : result.keySet()) {
                    ClusteringEngine.ClusteredPoints points = result.get(centroid);
                    System.out.println(" Centroid = " + centroid.toString() + " memberSize = " + points.getPoints()
                            .size());
                }

                System.out.println(
                        " For number of clusters = " + numClusters + " Cost = " + mlProblem.getCostFunction(result));

            } catch (Exception e) {
                e.printStackTrace();
                System.out.println("Exception in clustering data!");
            }
        } catch (IOException io) {
            logger.error("Error while reading the project properties file.", io);
        }
    }

    public static String configureLogging(boolean debug) {
        FileAppender fa = new FileAppender();

        if (!debug) {
            fa.setThreshold(Level.toLevel(Priority.INFO_INT));
            fa.setFile("executionLogs/log_infoLevel_report_" + Long.toString(System.currentTimeMillis()) + ".log");
        } else {
            fa.setThreshold(Level.toLevel(Priority.DEBUG_INT));
            fa.setFile("executionLogs/log_debugLevel_report_" + Long.toString(System.currentTimeMillis()) + ".log");
        }

        fa.setLayout(new EnhancedPatternLayout("%-6d [%25.35t] %-5p %40.80c - %m%n"));

        fa.activateOptions();
        org.apache.log4j.Logger.getRootLogger().addAppender(fa);
        return fa.getFile();
    }

    public static Properties getProjectProperties(String propertiesFilePath) throws IOException {
        logger.info("Properties file specified at location = " + propertiesFilePath);
        FileInputStream projFile   = new FileInputStream(propertiesFilePath);
        Properties      properties = new Properties();
        properties.load(projFile);
        return properties;
    }
}
