package com.formation.products.resource;

import com.formation.products.model.Product;
import com.formation.products.service.ProductService;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;

import java.net.URI;
import java.util.List;

@Path("/products")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class ProductResource {

    @Inject
    private ProductService productService;

    @Context
    private UriInfo uriInfo;

    @GET
    public Response getAllProducts(@QueryParam("category") String category) {
        List<Product> products;
        
        if (category != null && !category.trim().isEmpty()) {
            products = productService.getProductsByCategory(category);
        } else {
            products = productService.getAllProducts();
        }
        
        return Response.ok(products).build();
    }

    @GET
    @Path("/{id}")
    public Response getProduct(@PathParam("id") String id) {
        return productService.getProduct(id)
                .map(product -> Response.ok(product).build())
                .orElse(Response.status(Response.Status.NOT_FOUND)
                        .entity(new ErrorMessage("Product not found with id: " + id))
                        .build());
    }

    @POST
    public Response createProduct(Product product) {
        try {
            Product created = productService.createProduct(product);
            URI location = uriInfo.getAbsolutePathBuilder()
                    .path(created.getId())
                    .build();
            return Response.created(location).entity(created).build();
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new ErrorMessage(e.getMessage()))
                    .build();
        }
    }

    @PUT
    @Path("/{id}")
    public Response updateProduct(@PathParam("id") String id, Product product) {
        try {
            Product updated = productService.updateProduct(id, product);
            return Response.ok(updated).build();
        } catch (IllegalArgumentException e) {
            if (e.getMessage().contains("not found")) {
                return Response.status(Response.Status.NOT_FOUND)
                        .entity(new ErrorMessage(e.getMessage()))
                        .build();
            }
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new ErrorMessage(e.getMessage()))
                    .build();
        }
    }

    @PATCH
    @Path("/{id}/stock")
    public Response updateStock(@PathParam("id") String id, StockUpdate stockUpdate) {
        try {
            productService.updateStock(id, stockUpdate.getQuantity());
            return productService.getProduct(id)
                    .map(product -> Response.ok(product).build())
                    .orElse(Response.status(Response.Status.NOT_FOUND).build());
        } catch (IllegalArgumentException e) {
            if (e.getMessage().contains("not found")) {
                return Response.status(Response.Status.NOT_FOUND)
                        .entity(new ErrorMessage(e.getMessage()))
                        .build();
            }
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new ErrorMessage(e.getMessage()))
                    .build();
        }
    }

    @DELETE
    @Path("/{id}")
    public Response deleteProduct(@PathParam("id") String id) {
        productService.deleteProduct(id);
        return Response.noContent().build();
    }

    public static class ErrorMessage {
        private String message;

        public ErrorMessage() {}

        public ErrorMessage(String message) {
            this.message = message;
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }
    }

    public static class StockUpdate {
        private int quantity;

        public StockUpdate() {}

        public int getQuantity() {
            return quantity;
        }

        public void setQuantity(int quantity) {
            this.quantity = quantity;
        }
    }
}
