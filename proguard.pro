-dontoptimize
-dontobfuscate

-keepattributes *Annotation*
-keepattributes InnerClasses,EnclosingMethod
-keepattributes Signature

-keepdirectories    # for ressources

-keep,includedescriptorclasses class fr.nicopico.n2rss.** {
    *;
}
