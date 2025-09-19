build-deploy-image:
	@echo "Creating DB..."
	@docker rm -f quarkus-app
	@docker rm -f my-postgres
	@docker network create my-network || true
	@docker run --name my-postgres --network my-network -d -p 5432:5432 -e POSTGRES_USER=postgres -e POSTGRES_PASSWORD=postgres -e TZ=Asia/Kolkata -e PGTZ=Asia/Kolkata postgres
	@echo "Building the Maven project..."
	@mvn clean install
	@echo "Maven project built successfully."
	@echo "Building and deploying the image..."
	@docker build -f src/main/docker/Dockerfile -t my-image .
	@docker run -i --rm --name quarkus-app --network my-network -p 8080:8080 my-image
	@echo "Image built and deployed successfully."