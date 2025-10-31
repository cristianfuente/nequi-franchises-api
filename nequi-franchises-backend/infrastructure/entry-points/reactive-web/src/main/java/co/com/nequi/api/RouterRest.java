package co.com.nequi.api;

import co.com.nequi.api.handler.BranchHandler;
import co.com.nequi.api.handler.FranchiseHandler;
import co.com.nequi.api.handler.ProductHandler;
import org.springdoc.core.annotations.RouterOperation;
import org.springdoc.core.annotations.RouterOperations;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.reactive.function.server.HandlerFilterFunction;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;

import static org.springframework.web.reactive.function.server.RouterFunctions.route;

@Configuration
public class RouterRest {
    @Bean
    @RouterOperations({
            @RouterOperation(path = "/v1/franchises", method = RequestMethod.POST,
                    beanClass = FranchiseHandler.class, beanMethod = "create"),
            @RouterOperation(path = "/v1/franchises/{fid}", method = RequestMethod.GET,
                    beanClass = FranchiseHandler.class, beanMethod = "getById"),
            @RouterOperation(path = "/v1/franchises", method = RequestMethod.GET,
                    beanClass = FranchiseHandler.class, beanMethod = "getAll"),
            @RouterOperation(path = "/v1/franchises/{fid}/name", method = RequestMethod.PATCH,
                    beanClass = FranchiseHandler.class, beanMethod = "updateName"),
            @RouterOperation(path = "/v1/franchises/{fid}", method = RequestMethod.DELETE,
                    beanClass = FranchiseHandler.class, beanMethod = "delete"),

            @RouterOperation(path = "/v1/franchises/{fid}/branches", method = RequestMethod.POST,
                    beanClass = BranchHandler.class, beanMethod = "create"),
            @RouterOperation(path = "/v1/franchises/{fid}/branches", method = RequestMethod.GET,
                    beanClass = BranchHandler.class, beanMethod = "getByFranchise"),
            @RouterOperation(path = "/v1/branches/{bid}", method = RequestMethod.GET,
                    beanClass = BranchHandler.class, beanMethod = "getById"),
            @RouterOperation(path = "/v1/franchises/{fid}/branches/{bid}/name", method = RequestMethod.PATCH,
                    beanClass = BranchHandler.class, beanMethod = "updateName"),
            @RouterOperation(path = "/v1/franchises/{fid}/branches/{bid}", method = RequestMethod.DELETE,
                    beanClass = BranchHandler.class, beanMethod = "delete"),

            @RouterOperation(path = "/v1/franchises/{fid}/branches/{bid}/products", method = RequestMethod.POST,
                    beanClass = ProductHandler.class, beanMethod = "create"),
            @RouterOperation(path = "/v1/franchises/{fid}/branches/{bid}/products", method = RequestMethod.GET,
                    beanClass = ProductHandler.class, beanMethod = "listByBranch"),
            @RouterOperation(path = "/v1/franchises/{fid}/branches/{bid}/products/search", method = RequestMethod.GET,
                    beanClass = ProductHandler.class, beanMethod = "searchByName"),
            @RouterOperation(path = "/v1/franchises/{fid}/branches/{bid}/products/{pid}/name", method = RequestMethod.PATCH,
                    beanClass = ProductHandler.class, beanMethod = "updateName"),
            @RouterOperation(path = "/v1/franchises/{fid}/branches/{bid}/products/{pid}/stock", method = RequestMethod.PATCH,
                    beanClass = ProductHandler.class, beanMethod = "changeStock"),
            @RouterOperation(path = "/v1/franchises/{fid}/branches/{bid}/products/{pid}", method = RequestMethod.DELETE,
                    beanClass = ProductHandler.class, beanMethod = "deleteByBranch"),
            @RouterOperation(path = "/v1/franchises/{fid}/branches/top-products", method = RequestMethod.GET,
                    beanClass = ProductHandler.class, beanMethod = "topByFranchise")
    })
    public RouterFunction<ServerResponse> routerFunction(FranchiseHandler franchiseHandler,
                                                         BranchHandler branchHandler,
                                                         ProductHandler productHandler,
                                                         HandlerFilterFunction<ServerResponse, ServerResponse> errorFilter) {
        return route().filter(errorFilter).path("/v1", builder -> builder
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
                .GET("/franchises/{fid}/branches/{bid}/products/search", productHandler::searchByName)
                .PATCH("/franchises/{fid}/branches/{bid}/products/{pid}/name", productHandler::updateName)
                .PATCH("/franchises/{fid}/branches/{bid}/products/{pid}/stock", productHandler::changeStock)
                .DELETE("/franchises/{fid}/branches/{bid}/products/{pid}", productHandler::deleteByBranch)

                .GET("/franchises/{fid}/branches/top-products", productHandler::topByFranchise)
        ).build();
    }
}
