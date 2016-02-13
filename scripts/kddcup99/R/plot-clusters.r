library("rgl")

# ####################
# PREPARE CLUSTER DATA
# input file
file = "/tmp/KDD-Cup-1999/out/kmeans_1E-10_050_0050_03_L2NormV1_test.R_sample"
# read the entire data set
all_clusters_data <- read.csv(file)
# filter some of the clusters or not
clusters_data = subset(all_clusters_data, X0 >=0)
# clusters labels
clusters <- clusters_data[1]
# unique clusters
unique_clusters <- unique(clusters)
# unique cluster number
num_clusters <- nrow(unique_clusters)
#unlabeled data
data <- data.matrix(clusters_data[-c(1)])
random_projection <- matrix(data = rnorm(3*ncol(data)), ncol=3)
random_projection_norm <- random_projection / sqrt(rowSums(random_projection*random_projection))
projected_data <- data.frame(data %*% random_projection_norm)

# #################
# PLOT CLUSTER DATA
# open 3d window
open3d()
# resize window
par3d(windowRect = c(100, 100, 1200, 800))
# labels and points colors
colors = sapply(clusters, function(c) rainbow(num_clusters)[c])
# add legend
legend3d("topright", legend = paste('Cluster', as.matrix(unique_clusters)), pch = 16, col = colors, cex=1, inset=c(0.02))
# plot data
plot3d(projected_data, col = colors, size = 10)
# save snapshot
# snapshot3d(filename = '/tmp/3dplot.png', fmt = 'png')
