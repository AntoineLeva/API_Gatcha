# Utilisez une image de base avec Java et Maven
FROM maven:3.8.1-openjdk-11

# Copiez votre code source dans l'image Docker
COPY . /app
WORKDIR /app

# Construisez votre application avec Maven
RUN mvn clean install

# Exposez le port sur lequel votre application s'exécute
EXPOSE 8080

# Lancez votre application
CMD ["java", "-jar", "target/your-app.jar"]
