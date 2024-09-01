package de.upb.sse.cutNRun;

import org.springframework.boot.ApplicationArguments;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import sootup.core.inputlocation.AnalysisInputLocation;
import sootup.core.model.SourceType;
import sootup.java.bytecode.inputlocation.JavaClassPathAnalysisInputLocation;
import sootup.java.core.views.JavaView;

@Configuration
public class SootUpConfiguration {

    /*@Bean
    public JavaView javaViewForJunit(ApplicationArguments args){
        *//*String name = args.getNonOptionArgs().get(0);//get the first argument that isn't passed (without -- before it)
        //alternative: if it is passed as --personName=yourPersonName
        String name = args.getOptionValues("personName").get(0);*//*

        String pathToBinary = "src/test/resources/jars/spring-context-6.1.12.jar"; //slf4j-api-2.0.1 6
        *//*String pathToBinary = "src/test/resources/classes/";*//*
        AnalysisInputLocation inputLocation = new JavaClassPathAnalysisInputLocation(pathToBinary);
        return new JavaView(inputLocation);
    }*/

    /*@Bean
    public JavaView javaViewForSl4j(ApplicationArguments args){
        *//*String name = args.getNonOptionArgs().get(0);//get the first argument that isn't passed (without -- before it)
        //alternative: if it is passed as --personName=yourPersonName
        String name = args.getOptionValues("personName").get(0);*//*

        String pathToBinary = "src/test/resources/jars/junit-4.13.2.jar";
        *//*String pathToBinary = "src/test/resources/jars/";*//*
        AnalysisInputLocation inputLocation = new JavaClassPathAnalysisInputLocation(pathToBinary);
        return new JavaView(inputLocation);
    }*/
}
