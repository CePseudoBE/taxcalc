package be.hoffmann.backtaxes.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import io.swagger.v3.oas.models.tags.Tag;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {

    @Value("${server.port:8080}")
    private String serverPort;

    @Bean
    public OpenAPI backTaxesOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Back-Taxes API")
                        .description("API de calcul des taxes automobiles belges (TMC et taxe de circulation)")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("Back-Taxes")
                                .url("https://backtaxes.be"))
                        .license(new License()
                                .name("MIT")))
                .servers(List.of(
                        new Server()
                                .url("http://localhost:" + serverPort)
                                .description("Serveur local")))
                .tags(List.of(
                        new Tag().name("Taxes").description("Calcul des taxes automobiles (TMC et taxe annuelle)"),
                        new Tag().name("Calcul manuel").description("Calcul de taxes avec donnees manuelles"),
                        new Tag().name("Marques").description("Catalogue des marques automobiles"),
                        new Tag().name("Modeles").description("Catalogue des modeles de vehicules"),
                        new Tag().name("Variantes").description("Catalogue des variantes de vehicules"),
                        new Tag().name("Authentification").description("Inscription et connexion"),
                        new Tag().name("Soumissions").description("Soumission et moderation de vehicules"),
                        new Tag().name("Recherches sauvegardees").description("Gestion des recherches"),
                        new Tag().name("Administration taxes").description("Gestion des baremes et parametres"),
                        new Tag().name("Administration").description("Configuration des taxes")
                ));
    }
}
