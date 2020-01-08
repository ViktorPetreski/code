package com.fri.code.exercises.api.v1;

import com.kumuluz.ee.discovery.annotations.RegisterService;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.servers.Server;

import javax.ws.rs.ApplicationPath;
import javax.ws.rs.core.Application;
@ApplicationPath("/v1")
@RegisterService
@OpenAPIDefinition(info = @Info(title = "ExercisesAPI", version = "v1.0.0", contact = @Contact()), servers = @Server(url = "http://34.67.168.202:8080/v1"))
public class CodeApplication extends Application {

}