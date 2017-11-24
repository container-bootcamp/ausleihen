# Makefile to build the ausleihen scs
DATA_CONTAINER_NAME = ausleihen-sbt-cache
DATA_CONTAINER = $(shell docker ps -a --format "{{.Names}}" | grep $(DATA_CONTAINER_NAME))

BUILDER_IMAGE = quay.io/containerbootcamp/sbt-project-builder:latest

all: image

image: data-container
	docker run --rm \
    -v "$(CURDIR)":/sbt-project \
    -v /var/run/docker.sock:/var/run/docker.sock \
    -w /sbt-project	\
    --volumes-from $(DATA_CONTAINER_NAME) \
	--name ausleihen-scs-builder \
    $(BUILDER_IMAGE) sbt docker:publishLocal

data-container:
ifneq ($(DATA_CONTAINER),$(DATA_CONTAINER_NAME))
	@echo "Create sbt-cache container"
	docker run -d \
	-v /root/.sbt \
	-v /root/.ivy2 \
	--name ausleihen-sbt-cache \
	--entrypoint /bin/echo alpine
endif

image-publish: image
	docker push $(BUILDER_IMAGE)

clean-all: clean-data-container clean-project

clean-data-container:
ifeq ($(DATA_CONTAINER), $(DATA_CONTAINER_NAME))
	docker rm $(DATA_CONTAINER_NAME)
endif

clean-project:
	docker run --rm \
    -v "$(CURDIR)":/sbt-project \
    -v /var/run/docker.sock:/var/run/docker.sock \
    -w /sbt-project	\
    --volumes-from $(DATA_CONTAINER_NAME) \
	--name ausleihen-scs-builder \
    $(BUILDER_IMAGE) sbt clean

