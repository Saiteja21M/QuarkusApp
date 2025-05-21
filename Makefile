build-deploy-image:
	@echo "Creating DB..."
	@docker rm -f quarkus-app
	@docker rm -f my-postgres
	@docker run --name my-postgres --network my-network -d -p 5433:5433 -e POSTGRES_USER=postgres -e POSTGRES_PASSWORD=postgres postgres:15
	@echo "Building the Maven project..."
	@mvn clean install
	@echo "Maven project built successfully."
	@echo "Building and deploying the image..."
	@docker build -f src/main/docker/Dockerfile -t my-image .
	@docker run -i --rm --name quarkus-app --network my-network -p 8080:8080 my-image
	@echo "Image built and deployed successfully."