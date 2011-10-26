TARGETS= 837-flow.png

all: clean $(TARGETS)

clean:
	rm -rf $(TARGETS)

%.png: %.ditaa
	java -jar ditaa.jar "$<" "$@"

