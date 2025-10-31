package co.com.nequi.api;

import co.com.nequi.api.handler.BranchHandler;
import co.com.nequi.api.handler.FranchiseHandler;
import co.com.nequi.api.handler.ProductHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;

import static org.springframework.web.reactive.function.server.RouterFunctions.route;

@Configuration
public class RouterRest {
    @Bean
    public RouterFunction<ServerResponse> routerFunction(FranchiseHandler franchiseHandler,
                                                         BranchHandler branchHandler,
                                                         ProductHandler productHandler) {
        return route().path("/v1", builder -> builder
                .POST("/franchises", franchiseHandler::create)
                .GET("/franchises/{fid}", franchiseHandler::getById)
                .GET("/franchises", franchiseHandler::getAll)
                .PATCH("/franchises/{fid}/name", franchiseHandler::updateName)
                .DELETE("/franchises/{fid}", franchiseHandler::delete)

                .POST("/franchises/{fid}/branches", branchHandler::create)
                .GET("/franchises/{fid}/branches", branchHandler::getByFranchise)
                .GET("/branches/{bid}", branchHandler::getById)
                .PATCH("/franchises/{fid}/branches/{bid}/name", branchHandler::updateName)
                .DELETE("/franchises/{fid}/branches/{bid}", branchHandler::delete)

                .POST("/franchises/{fid}/branches/{bid}/products", productHandler::create)
                .GET("/franchises/{fid}/branches/{bid}/products", productHandler::listByBranch)
                .PATCH("/franchises/{fid}/branches/{bid}/products/{pid}/name", productHandler::updateName)
                .PATCH("/franchises/{fid}/branches/{bid}/products/{pid}/stock", productHandler::changeStock)
                .DELETE("/franchises/{fid}/branches/{bid}/products/{pid}", productHandler::deleteByBranch)

                .GET("/franchises/{fid}/branches/top-products", productHandler::topByFranchise)
        ).build();
    }
}
