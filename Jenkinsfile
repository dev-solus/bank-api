#!groovy
node {
  def apps = [
    [name: "rafeek-api", port: '8080', exposed_port: '8081', path: '', domaine: "", volume: "/uploads",domaine_prefix: ''],
  ];

  def app

  stage('deploy all') {
    def commit = checkout scm
   
    def currentCommit = commit.GIT_COMMIT // sh(script: "git rev-parse HEAD", returnStdout: true).trim()
    def lastCommit = commit.GIT_PREVIOUS_SUCCESSFUL_COMMIT

   // lastCommit = lastCommit == null || lastCommit.trim() == '' ? 'HEAD^' : lastCommit

  //  def command = "git diff-tree -r --name-only ${currentCommit} ${lastCommit}"

    apps.each { e ->

     /* if(!lastCommit) {
        echo "First commit building ${e.name} ..."
        return
      }*/

      // def changes = sh(script: "${command} -- ./${e.name}", returnStdout: true).trim()

      // println("***********************> ${changes}")

      // if (!changes) {
      //   echo "No changes in the folder ${e.name}."
      //   // return
      // }

      // echo "Changes found in the folder ${e.name}:"
      // echo "${changes}"

      app = docker.build("${e.name}", "-t ${e.name} -f ./Dockerfile ./")
      // sh """docker build -t sa-dev-api-cms -f ./cmsApi/Dockerfile ./"""

      sh "docker rm --force ${e.name}"

      if (e.exposed_port == "") {
         sh """docker run -d \
          --restart unless-stopped \
          --network proxy \
          --volume /home/dev/volumes/${e.name}/uploads:/app/uploads \
          --label traefik.enable=true \
          --label traefik.http.routers.${e.name}.tls=true \
          --label traefik.http.routers.${e.name}.tls.certresolver=letsencrypt \
          --label traefik.http.routers.${e.name}.rule='Host(`${e.domaine}`) '\
          --label traefik.http.services.${e.name}.loadbalancer.server.port=${e.port} \
          --name ${e.name} \
          ${e.name}"""
      } else {
         sh """docker run -d \
          --restart unless-stopped \
          --publish ${e.exposed_port}:${e.port} \
          --volume /home/dev/volumes/${e.name}/wwwroot:/app/wwwroot \
          --name ${e.name} \
          ${e.name}"""
      }

      sh "docker rmi --force ${e.name}"
    }
  }
}
