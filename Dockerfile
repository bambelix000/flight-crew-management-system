FROM mcr.microsoft.com/devcontainers/dotnet:8.0

# Node.js
ARG NODE_VERSION="20"
RUN su vscode -c "source /usr/local/share/nvm/nvm.sh && nvm install ${NODE_VERSION} 2>&1"

# .NET
RUN su vscode -c "dotnet tool install -g dotnet-ef"
ENV PATH="$PATH:/home/vscode/.dotnet/tools"

RUN apt-get update && apt-get install -y curl git && rm -rf /var/lib/apt/lists/*