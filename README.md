# Docuburst

DocuBurst is the first visualization of document content which takes advantage of the human-created structure in lexical databases. We use an accepted design paradigm to generate visualizations which improve the usability and utility of WordNet as the backbone for document content visualization. A radial, space-filling layout of hyponymy (IS-A relation) is presented with interactive techniques of zoom, filter, and details-on-demand for the task of document visualization. The techniques can be generalized to multiple documents.

Created by Christopher Collins. Currently maintained by Rafael Veras. More info at [vialab](http://vialab.science.uoit.ca/portfolio/docuburst).

**Download the [latest release](https://github.com/vialab/docuburst-desktop/releases/download/v1.0-alpha/docuburst-v1.0-alpha.jar).**

##How to run

`java -jar docuburst.jar my-plain-text-doc.txt` (requires Java 8+)

##Getting Started

1. Type the word you want to be the root of the tree. It defines the scope of your investigation. Not sure? Type *entity* for the broadest scope.
2. Go to *Options* and play with *Maximum tree depth*. It controls the level of detail of the view.
3. Now click *Single node* to color only the categories that occur *directly* in the document. 
4. Click on one of these categories and go to *Concordance lines* to see the excerpts where they appear in the text. *Text segments* is similar, but shows you more context.
5. By the way, the text is divided into segments based on vocabulary changes (check Marti Hearst's awesome [paper](http://people.ischool.berkeley.edu/~hearst/papers/cl-texttiling97.pdf)). These segments are represented by tiles on the right side. When you click a category, the tiles where it appears become orange!
6. Pick a category of interest and double click it to narrow down your exploration.
7. LeftClick+Hold+Move on a blank area to pan the view. RightClick+Hold+Move to zoom in/out.
8. Double RightClick on the root (center of the sunburst) to roll-up.

##Citation

C. Collins, S. Carpendale, and G. Penn, “DocuBurst: Visualizing Document Content Using Language Structure,” Computer Graphics Forum (Proc. of the Eurographics/IEEE-VGTC Symposium on Visualization (EuroVis)), vol. 28, iss. 3, pp. 1039-1046, 2009. 

![Docuburst](http://vialab.science.uoit.ca/wp-content/uploads/2011/12/docuburst_idea_search_pl1.png)


