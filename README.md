# SIMG-VIZ
A new visualization system for entity resolution and clustering that allows us to investigate different match and clustering techniques for multi-source entity resolution. SIMG-VIZ offers the following
key features:
* SIMG-VIZ allows a user to analyze precomputed similarity
graphs and clusterings from existing ER tools and also
supports executing and analyzing ER match tasks directly
with FAMER.
* Different graph and ER cluster visualization techniques
and layouts can be applied to choose the best visualiza-
tions.
* To increase performance, some layouts can be precom-
puted on the server with either parallel or serial computa-
tion. This provides a significant optimization potential in
particular for force-directed layouts [3].
* To support visualization of large graphs, preprocessing
techniques such as sampling (also executed in parallel on
the server) can be selected to obtain a fast overview of
large similarity graphs and their clustering results.
* Clusters and their overlaps as well as edges annotated
with their type and similarity are visualized by using a
simple but useful cake-like visual metaphor. Users can
interact with clusters and select individual clusters for
investigation.

## Usage
Based on Flink, this software extracts and visualizes a cluster or a cluster together with is neighboring clusters from a large graph. The input graph for this software is similar to input graph of [Gradoop](https://github.com/dbs-leipzig/gradoop). An assumption is that each vertex has the property "ClusterId" which shows to which cluster that vertex belongs. This property can contain several cluster ids separated by a comma. To use the software, you need first to copy your data into the directory "src/main/resources/data" under an existing folder (called category) or a new folder. After updating the maven, the software can be run just by running the Server class. 

[The corresponding Paper:](https://dbs.uni-leipzig.de/file/edbt-demopaper.pdf EDBT2018)
