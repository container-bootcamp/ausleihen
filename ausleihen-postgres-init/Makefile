IMAGE_VERSION = latest
IMAGE_NAME = quay.io/containerbootcamp/ausleihen-pg-init:$(IMAGE_VERSION)
all:  image

image:
	docker build -t $(IMAGE_NAME) .

image-push: image
	docker push $(IMAGE_NAME)