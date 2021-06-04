package com.pipeline;

import java.io.File;

import com.comon.Constants;
import com.git.*;
import com.featureExtractionRefined.*;
import com.warningClassification.*;


public class Pipeline {
    public static void main(String[] args) {

		// For some reason, the code below throws error whereas calling the main functions from the command line does not
        
        LogParser.main(null);

        OverallFeatureExtraction.main(null);

        FeatureRefinement.main(null);

    }
}
