# Requirements

* **[Docker engine](https://docs.docker.com/engine/installation/)**
* **[Oracle JDK 8](https://docs.oracle.com/javase/8/docs/technotes/guides/install/install_overview.html)** or **[Open JDK 8](http://openjdk.java.net/install/)**

# How to build NGB docker image

Obtain the source code from github:
```
$ git clone https://github.com/epam/NGB.git
$ cd NGB
```

## Build NGB server jar

Build war

```
$ ./gradlew buildJar
```

Copy jar to docker folder

```
$ cp dist/catgenome.jar docker/core/catgenome.jar
```

## Build NGB Command Line Interface tarball

Build CLI

```
$ ./gradlew buildCli
```

Copy CLI to docker folder

```
$ cp dist/ngb-cli.tar.gz docker/core/ngb-cli.tar.gz
```

## Build docker image

Navigate to dockerfile location

```
$ cd docker/core
```

Build docker image

```
$ docker build -t ngb:latest .
```

Cleanup docker folder

```
$ rm catgenome.jar && rm ngb-cli.tar.gz
```

# How to run NGB docker image

To run an image, that was built in a previous section, run the following command

```
docker run -p 8080:8080 -d --name ngbcore ngb:latest
```

This command performs the following operations
* Create and start the container in a background mode
* Map port 8080 of the container to port 8080 of the host 
* Make container accessible by name `ngbcore`

Verify that NGB is up and running: navigate with your web-browser (**Chrome** prefered) to [http://localhost:8080/catgenome](http://localhost:8080/catgenome)

# Latest stable and demo docker images

Latest stable NGB version is available at [DockerHub Repository](https://hub.docker.com/r/lifescience/ngb/)

There are two version of NGB in this repository:
* **ngb:latest** - a "core" version - contains image of NGB without any data in it, only binaries
* **ngb:latest-demo** - a "demo" version - contains demo data set, which does not require any data registration, you need only to run an image

Details on usage of these images are available in [DockerHub Readme](https://hub.docker.com/r/lifescience/ngb/)

# Develop versions of docker images

Docker images from develop branch are automatically builded by and pushed to DockerHub. 
Due to the large size of demo data and Travis disk space limitations the demo data could not be included in the image.  
* **ngb:-dev** - contains image of NGB without any data in it, only binaries

To download the demo data just pull the -dev docker image

```
$ docker pull lifescience/ngb:-dev 
```

Launch the NGB from a pulled image

Replace <script_folder> placeholder with a real path to the folder demo_data_download.sh script and run
```
$ docker run -p 8080:8080 -d --name ngbcore -v <script_folder>:/demo_data ngb:-dev
```
This command will start docker with name ngbcore, mount the folder containing demo_data_download script to the /demo_data mount point and expose the 8080 port for the ngb graphical interface

Now run the script to download demo data 
```
docker exec -d ngb_core ./demo_data/demo_data_download.sh
```
The demo_data_download.sh script will download genome references to ~/ngb_data/references and demo data to ~/ngb_data/data_files
But you can change the paths for references and data files by passing -r and -f arguments, respectively

For example:
```
./demo_data_download.sh -r ~/references -f ~/some/other/folder  
```
The script will download all the necessary files and will register them by itself. But be  patient, please, as the reference donwload may take much time. 

