LIBS=xz/configure libarchive/configure hfst-ospell/configure

all: $(LIBS)
	abuild abuild.json -o ../libs
	cp hfst-ospell/libhfstospell.jar ../libs

xz/configure:
	cd $(dir $@) && ./autogen.sh

libarchive/configure:
	cd $(dir $@) && ./build/autogen.sh

hfst-ospell/configure:
	cd $(dir $@) && ./autogen.sh

clean:
	for i in $(dir $(LIBS)); do pushd $$i; git clean -fdx; popd; done
