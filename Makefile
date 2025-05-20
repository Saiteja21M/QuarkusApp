build-deploy-image:
	@echo "Building and deploying the image..."
	@docker build -f src/main/docker/Dockerfile -t my-image .
	@docker run -p 8080:80 my-image
	@echo "Image built and deployed successfully."

mvn-build:
	@echo "Building the Maven project..."
	@mvn clean install
	@echo "Maven project built successfully."