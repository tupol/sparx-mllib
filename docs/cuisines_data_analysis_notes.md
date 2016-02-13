## Data analysis notes

|||
| :---------------------------- | ------------: |
| Total training data size      | 39774 records |
| Training / Testing split      |     80% / 20% |
| Cuisines (labels / classes)   |            20 |
| Ingredients (features)        |          6703 |

### Total ingredients per cuisine
| Cuisine                                  | Ingredients #   |
| :----------------------------------------| --------------: |
| italian                                  |            2928 |
| mexican                                  |            2681 |
| southern_us                              |            2459 |
| french                                   |            2100 |
| chinese                                  |            1791 |
| indian                                   |            1664 |
| cajun_creole                             |            1575 |
| japanese                                 |            1439 |
| thai                                     |            1376 |
| spanish                                  |            1263 |
| greek                                    |            1198 |
| british                                  |            1165 |
| vietnamese                               |            1108 |
| irish                                    |             999 |
| moroccan                                 |             974 |
| filipino                                 |             947 |
| korean                                   |             898 |
| jamaican                                 |             877 |
| russian                                  |             872 |
| brazilian                                |             853 |

### Max ingredients used in a recipe per cuisine

| Ingredient                               | Max Ingr.       |
| :--------------------------------------- | --------------: |
| italian                                  |              65 |
| brazilian                                |              59 |
| mexican                                  |              52 |
| indian                                   |              49 |
| southern_us                              |              40 |
| thai                                     |              40 |
| chinese                                  |              38 |
| filipino                                 |              38 |
| jamaican                                 |              35 |
| spanish                                  |              35 |
| japanese                                 |              34 |
| french                                   |              31 |
| vietnamese                               |              31 |
| cajun_creole                             |              31 |
| moroccan                                 |              31 |
| british                                  |              30 |
| korean                                   |              29 |
| irish                                    |              27 |
| greek                                    |              27 |
| russian                                  |              25 |

Thoughts: Recipes with more than 40 ingredients are suspicious; corrupted data? 
Maybe just garbage data. Who cooks with 65 ingredients (except Heston Blumenthal, of course)?

### Top 20 ingredients

| Ingredient                               | Occurrences     |
| :--------------------------------------- | --------------: |
| salt                                     |           18049 |
| olive oil                                |            7972 |
| onions                                   |            7972 |
| water                                    |            7457 |
| garlic                                   |            7380 |
| sugar                                    |            6434 |
| garlic cloves                            |            6237 |
| butter                                   |            4848 |
| ground black pepper                      |            4785 |
| all-purpose flour                        |            4632 |
| pepper                                   |            4438 |
| vegetable oil                            |            4385 |
| eggs                                     |            3388 |
| soy sauce                                |            3296 |
| kosher salt                              |            3113 |
| green onions                             |            3078 |
| tomatoes                                 |            3058 |
| large eggs                               |            2948 |
| carrots                                  |            2814 |
| unsalted butter                          |            2782 |

### Bottom 20 ingredients
| Ingredient                               | Occurrences     |
| :----------------------------------------| --------------: |
| zatarain’s jambalaya mix                 |               1 |
| zatarains creole seasoning               |               1 |
| yucca root                               |               1 |
| young nettle                             |               1 |
| young leeks                              |               1 |
| yoplait® greek 2% caramel yogurt         |               1 |
| yoplait® greek 100 blackberry pie yogurt |               1 |
| yoplait                                  |               1 |
| yogurt low fat                           |               1 |
| yogurt dressing                          |               1 |
| yoghurt natural low fat                  |               1 |
| yellowtail snapper fillets               |               1 |
| yellowtail                               |               1 |
| yellowfin                                |               1 |
| yellow heirloom tomatoes                 |               1 |
| yam noodles                              |               1 |
| yam bean                                 |               1 |
| xuxu                                     |               1 |
| worcestershire sauce low sodium          |               1 |
| wood mushrooms                           |               1 |

### Total recipes per cuisine
| Cuisine                                  | Recipes         |
| :----------------------------------------| --------------: |
| italian                                  |            7838 |
| mexican                                  |            6438 |
| southern_us                              |            4320 |
| indian                                   |            3003 |
| chinese                                  |            2673 |
| french                                   |            2646 |
| cajun_creole                             |            1546 |
| thai                                     |            1539 |
| japanese                                 |            1423 |
| greek                                    |            1175 |
| spanish                                  |             989 |
| korean                                   |             830 |
| vietnamese                               |             825 |
| moroccan                                 |             821 |
| british                                  |             804 |
| filipino                                 |             755 |
| irish                                    |             667 |
| jamaican                                 |             526 |
| russian                                  |             489 |
| brazilian                                |             467 |


## MLlib Model Evaluation Results

### LogisticRegressionModel model evaluation

| Parameter                    | Value     |
| :--------------------------- | --------: |
| Precision                    |  68.3065% |
| Error                        |  31.6935% |
| Weighted Precision           |  68.6838% |
| Weighted True Positive Rate  |  68.3065% |
| Weighted False Positive Rate |   2.7189% |

| Cuisine              | TPR       | FPR       | Prec.     | Error     |
| :------------------- | --------: | --------: | --------: | --------: |
| italian              |  76.0169% |   4.1829% |  81.8804% |  18.1196% | 
| french               |  45.8333% |   3.5936% |  45.2055% |  54.7945% | 
| spanish              |  36.2179% |   1.8979% |  34.0361% |  65.9639% | 
| british              |  41.2955% |   1.4305% |  38.0597% |  61.9403% | 
| mexican              |  85.7881% |   3.0758% |  84.4784% |  15.5216% | 
| cajun_creole         |  57.0833% |   1.6269% |  59.6950% |  40.3050% | 
| greek                |  61.7391% |   1.3471% |  57.8804% |  42.1196% | 
| moroccan             |  62.2951% |   0.6203% |  67.8571% |  32.1429% | 
| southern_us          |  66.0756% |   4.5196% |  64.2429% |  35.7571% | 
| jamaican             |  62.6761% |   0.3160% |  70.6349% |  29.3651% | 
| vietnamese           |  49.5968% |   1.1893% |  47.1264% |  52.8736% | 
| thai                 |  65.6051% |   1.2830% |  67.9121% |  32.0879% | 
| indian               |  80.5464% |   1.7740% |  79.1622% |  20.8378% | 
| russian              |  34.5588% |   0.7682% |  34.3066% |  65.6934% | 
| irish                |  46.6667% |   1.1582% |  40.2655% |  59.7345% | 
| korean               |  71.7131% |   0.6983% |  68.9655% |  31.0345% | 
| japanese             |  69.8068% |   1.4864% |  62.9630% |  37.0370% | 
| brazilian            |  51.4706% |   0.5890% |  50.3597% |  49.6403% | 
| chinese              |  71.6883% |   2.1839% |  69.5214% |  30.4786% | 
| filipino             |  48.4979% |   0.6714% |  59.1623% |  40.8377% | 

| Legend ||
| ------ | ----------------------- |
| TPR    | True Positive Rate      |
| FPR    | False Positive Rate     |
| Prec.  | Precision (TP / LC)     |
| TP     | True Positive by Class  |
| LC     | Label Count By Class    |

Training was completed in 00:35 (minutes:seconds).

### NaiveBayesModel model evaluation

| Parameter                    | Value     |
| :--------------------------- | --------: |
| Precision                    |  73.7076% |
| Error                        |  26.2924% |
| Weighted Precision           |  75.2382% |
| Weighted True Positive Rate  |  73.7076% |
| Weighted False Positive Rate |   3.1373% |

| Cuisine              | TPR       | FPR       | Prec.     | Error     |
| :------------------- | --------: | --------: | --------: | --------: |
| italian              |  85.7202% |   6.1140% |  77.8401% |  22.1599% | 
| french               |  59.8002% |   3.5840% |  54.1243% |  45.8757% | 
| spanish              |  29.3729% |   0.3129% |  70.6349% |  29.3651% | 
| british              |  29.1845% |   0.5296% |  51.9084% |  48.0916% | 
| mexican              |  90.3291% |   2.1765% |  88.9776% |  11.0224% | 
| cajun_creole         |  70.8511% |   1.2437% |  69.6653% |  30.3347% | 
| greek                |  53.0086% |   0.4414% |  78.0591% |  21.9409% | 
| moroccan             |  70.2381% |   0.2863% |  83.8863% |  16.1137% | 
| southern_us          |  78.4791% |   7.9896% |  54.4304% |  45.5696% | 
| jamaican             |  45.3333% |   0.0835% |  87.1795% |  12.8205% | 
| vietnamese           |  36.1789% |   0.2356% |  76.0684% |  23.9316% | 
| thai                 |  82.1285% |   1.5304% |  69.6763% |  30.3237% | 
| indian               |  90.0789% |   1.5211% |  82.3711% |  17.6289% | 
| russian              |  17.5758% |   0.0251% |  90.6250% |   9.3750% | 
| irish                |  20.6349% |   0.0921% |  78.0000% |  22.0000% | 
| korean               |  58.1028% |   0.0674% |  94.8387% |   5.1613% | 
| japanese             |  59.1346% |   0.2476% |  89.4545% |  10.5455% | 
| brazilian            |  29.6552% |   0.1502% |  70.4918% |  29.5082% | 
| chinese              |  88.1919% |   2.5716% |  71.1310% |  28.8690% | 
| filipino             |  51.8828% |   0.2271% |  82.1192% |  17.8808% | 

| Legend ||
| ------ | ----------------------- |
| TPR    | True Positive Rate      |
| FPR    | False Positive Rate     |
| Prec.  | Precision (TP / LC)     |
| TP     | True Positive by Class  |
| LC     | Label Count By Class    |

Training was completed in 00:00 (minutes:seconds).


### DecisionTreeModel model evaluation

| Parameter                    | Value     |
| :--------------------------- | --------: |
| Precision                    |  44.6427% |
| Error                        |  55.3573% |
| Weighted Precision           |  59.6803% |
| Weighted True Positive Rate  |  44.6427% |
| Weighted False Positive Rate |   7.7065% |

| Cuisine              | TPR       | FPR       | Prec.     | Error     |
| :------------------- | --------: | --------: | --------: | --------: |
| italian              |  63.9429% |  12.0248% |  55.9591% |  44.0409% | 
| french               |   0.9756% |   0.0985% |  42.1053% |  57.8947% | 
| spanish              |   2.6846% |   0.1026% |  40.0000% |  60.0000% | 
| british              |   0.8230% |   0.0170% |  50.0000% |  50.0000% | 
| mexican              |  61.6913% |   3.0010% |  80.0926% |  19.9074% | 
| cajun_creole         |   5.6769% |   0.0520% |  81.2500% |  18.7500% | 
| greek                |  20.8211% |   0.2403% |  71.7172% |  28.2828% | 
| moroccan             |  20.5534% |   0.2044% |  68.4211% |  31.5789% | 
| southern_us          |  85.0496% |  41.5465% |  20.0792% |  79.9208% | 
| jamaican             |   7.7922% |   0.0084% |  92.3077% |   7.6923% | 
| vietnamese           |  16.4609% |   0.2298% |  59.7015% |  40.2985% | 
| thai                 |  35.2201% |   1.0333% |  58.5366% |  41.4634% | 
| indian               |  51.4377% |   0.7418% |  85.4867% |  14.5133% | 
| russian              |   0.0000% |   0.0000% |   0.0000% | 100.0000% | 
| irish                |   5.3659% |   0.0339% |  73.3333% |  26.6667% | 
| korean               |  28.3951% |   0.2043% |  74.1935% |  25.8065% | 
| japanese             |  18.9252% |   0.1470% |  82.6531% |  17.3469% | 
| brazilian            |   0.0000% |   0.0338% |   0.0000% | 100.0000% | 
| chinese              |  59.4561% |   3.1831% |  57.4671% |  42.5329% | 
| filipino             |  18.3168% |   0.1611% |  66.0714% |  33.9286% | 

| Legend ||
| ------ | ----------------------- |
| TPR    | True Positive Rate      |
| FPR    | False Positive Rate     |
| Prec.  | Precision (TP / LC)     |
| TP     | True Positive by Class  |
| LC     | Label Count By Class    |

Training was completed in 03:06 (minutes:seconds).

### RandomForest model evaluation

| Parameter                    | Value     |
| :--------------------------- | --------: |
| Precision                    |  30.7951% |
| Error                        |  69.2049% |
| Weighted Precision           |  46.1824% |
| Weighted True Positive Rate  |  30.7951% |
| Weighted False Positive Rate |  16.6927% |

| Cuisine              | TPR       | FPR       | Prec.     | Error     |
| :------------------- | --------: | --------: | --------: | --------: |
| italian              |  99.1898% |  83.3159% |  22.5913% |  77.4087% | 
| french               |   0.0000% |   0.0000% |   0.0000% | 100.0000% | 
| spanish              |   0.0000% |   0.0000% |   0.0000% | 100.0000% | 
| british              |   0.0000% |   0.0000% |   0.0000% | 100.0000% | 
| mexican              |  35.8510% |   0.9169% |  88.6675% |  11.3325% | 
| cajun_creole         |   0.0000% |   0.0000% |   0.0000% | 100.0000% | 
| greek                |   0.0000% |   0.0000% |   0.0000% | 100.0000% | 
| moroccan             |   0.0000% |   0.0000% |   0.0000% | 100.0000% | 
| southern_us          |   8.1413% |   0.6221% |  61.6279% |  38.3721% | 
| jamaican             |   0.0000% |   0.0000% |   0.0000% | 100.0000% | 
| vietnamese           |   0.7968% |   0.0000% | 100.0000% |   0.0000% | 
| thai                 |   5.7692% |   0.0612% |  79.4118% |  20.5882% | 
| indian               |  32.2654% |   0.2628% |  90.6752% |   9.3248% | 
| russian              |   0.0000% |   0.0000% |   0.0000% | 100.0000% | 
| irish                |   0.0000% |   0.0000% |   0.0000% | 100.0000% | 
| korean               |   0.0000% |   0.0000% |   0.0000% | 100.0000% | 
| japanese             |   1.6279% |   0.0000% | 100.0000% |   0.0000% | 
| brazilian            |   0.0000% |   0.0000% |   0.0000% | 100.0000% | 
| chinese              |  26.3764% |   0.7188% |  72.0280% |  27.9720% | 
| filipino             |   0.0000% |   0.0000% |   0.0000% | 100.0000% | 

| Legend ||
| ------ | ----------------------- |
| TPR    | True Positive Rate      |
| FPR    | False Positive Rate     |
| Prec.  | Precision (TP / LC)     |
| TP     | True Positive by Class  |
| LC     | Label Count By Class    |

Training was completed in 01:21 (minutes:seconds).


[Back](cuisines.md)
