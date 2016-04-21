# encoding: utf-8
from nltk.corpus import brown, stopwords
from nltk.tokenize.texttiling import TextTilingTokenizer
from nltk.tag import pos_tag, pos_tag_sents
from nltk import word_tokenize
import codecs
from argparse import ArgumentParser
import os

argparser = ArgumentParser()
argparser.add_argument('file', help="text document")
args = argparser.parse_args()


stopwords = stopwords.words('english')

doc_path = os.path.splitext(args.file)[0]

tt = TextTilingTokenizer()
text = codecs.open(doc_path + '.txt', 'r', "utf-8").read()
parags = tt.tokenize(text)


buffer_tiled = ''
buffer_tiled_tagged = ''
buffer_tiled_tagged_clean = ''

tagged_parags = pos_tag_sents([word_tokenize(p) for p in parags])
clean_parags  = [filter(lambda taggedword: taggedword[0] not in stopwords, p) for p in tagged_parags]

for i, p in enumerate(parags):
    buffer_tiled += p

    for word, tag in tagged_parags[i]:
        buffer_tiled_tagged += word + "/" + tag + ' '
        if word not in stopwords:
            if tag[0] == 'V': tag_abstract = 'verb'
            elif tag[0] == 'N': tag_abstract = 'noun'
            else: continue
            buffer_tiled_tagged_clean += word + ' ' + tag_abstract + '\n'

    if (i < len(parags) - 1):
        buffer_tiled += "\n==========\n\n"
        buffer_tiled_tagged += "\n==========\n\n"
    buffer_tiled_tagged_clean += "==========\n"    

doc_tiled = codecs.open(doc_path + ".tiled.txt", 'w', 'utf-8')
doc_tiled_tagged = codecs.open(doc_path + ".tiled.tagged.txt", 'w', 'utf-8')
doc_tiled_tagged_clean = codecs.open(doc_path + ".tiled.tagged.cleaned.txt", 'w', 'utf-8')

doc_tiled.write(buffer_tiled)
doc_tiled_tagged.write(buffer_tiled_tagged)
doc_tiled_tagged_clean.write(buffer_tiled_tagged_clean)

doc_tiled.close()
doc_tiled_tagged.close()
doc_tiled_tagged_clean.close()

