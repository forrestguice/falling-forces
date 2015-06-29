package:  com.forrestguice.glstuff.gle

Files in this package originate from "factious" (http://fractiousg.blogspot.com/2012/04/rendering-text-in-opengl-on-android.html)
who posted an in-depth article on rendering text with OpenGL on Android. This code was released under the CC0 1.0 public domain license.

I followed his article step-by-step and reproduced the code he provided - its a mash of copypaste and
my own refactoring as I worked through the material and integrated it with preexisting "convenience classes"
I was already using (looking at you Texture and TextureRegion). 

This implementation is also released under the CC0 1.0 public domain license.
http://creativecommons.org/publicdomain/zero/1.0/legalcode

TODO:
This package could use refactoring, or be entirely replaced with one of the other implementations
of the gltext article - there are updates to the article (dated 2013) that indicate these implementations
exist on github.

TODO: verify the licenses are all compatible, and replace tight coupling to files of differing license (if required) .. it shouldn't be a problem but IANAL
If it is a problem its fairly trivial to strip the gltext code from the project and replace
it with a more recent implementation (bound to be something with compatible license on github).
