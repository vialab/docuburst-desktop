Java API

~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
MontyTagger - A Brill-Based POS Tagger for Python/Java
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

# Author: Hugo Liu <hugo@media.mit.edu>
# Project Page: <http://web.media.mit.edu/~hugo/montytagger>
# 
# Copyright (c) 2002 by Hugo Liu, MIT Media Lab
# Original Brill data (c) Eric Brill, UPenn, M.I.T.
#
# Use is granted under the GNU General Public License (GPL):
# <http://www.gnu.org/licenses/gpl.html>
#
#   - Java API:
#       - Tag(String text)
#           - use this to tokenize & tag text
#           - returns text in word/NN format
#       - TagTokenized(String tagged)
#           - use this to tag already tokenized text
#   - To use from Java code:
#       1. make sure "montytagger.jar" is
#         in your class path, in addition to 3 data files
#       2. in your code, you need something like:
#
#       import montytagger.JMontyTagger; // loads namespace
#       public class YourClassHere {
#         public static JMontyTagger j = new JMontyTagger();
#         public yourFunction(String raw, String toked) {
#            tagged1 = j.Tag(raw) // tokenizes,tags raw text
#            tagged2 = j.TagTokenized(raw) // tags pretoked
#
#