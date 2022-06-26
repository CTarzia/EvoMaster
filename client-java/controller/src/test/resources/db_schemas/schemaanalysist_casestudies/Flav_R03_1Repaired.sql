-- http://www.ars.usda.gov/Services/docs.htm?docid=6231
CREATE TABLE DATA_SRC (DataSrc_ID TEXT, Authors TEXT, Title TEXT, Jorunal TEXT, Year TEXT, Volume TEXT, Issue TEXT, Start_Page TEXT, Emd_Page TEXT);
CREATE TABLE DATSRCLN (NDB_No TEXT, Nutr_no TEXT, DataSrc_ID TEXT);
CREATE TABLE FD_GROUP (FdGrp_CD TEXT, FdGrp_Desc TEXT);
CREATE TABLE FLAV_DAT (NDB_No TEXT, Nutr_no TEXT, Nutrient_name TEXT, Flav_Val DECIMAL, Se DECIMAL, n INTEGER, Min DECIMAL, CC TEXT, Max DECIMAL);
CREATE TABLE FLAV_IND (NDB_No TEXT, DataSrc_ID TEXT, Food_No TEXT, FoodIndiv_Desc TEXT, Method TEXT, Cmpd_Name TEXT, Rptd_CmpdVal DECIMAL, Rptd_Std_Dev TEXT, Num_Data_Pts DECIMAL, LT TEXT, Rptd_Units TEXT, Fresh_Dry_Wt TEXT, Quant_Std TEXT, Conv_Factor_G DECIMAL, Conv_Factor_M TEXT, Conv_Factor_SpGr DECIMAL, Cmpd_Val DECIMAL, Cmpt_StdDev TEXT);
CREATE TABLE FOOD_DES (NDB_No TEXT, FdGrp_Cd TEXT, Long_Desc TEXT, SciName TEXT);
CREATE TABLE NUTR_DEF (Nutrr_no TEXT, Nutrient_name TEXT, Flav_Class TEXT, Unit TEXT, Tagname TEXT);
CREATE UNIQUE INDEX DATA_SRC_PrimaryKey ON DATA_SRC (DataSrc_ID );
CREATE INDEX DATSRCLN_DATA_SRCDATSRCLN ON DATSRCLN (DataSrc_ID );
CREATE INDEX DATSRCLN_DATSRCLNNDB_No ON DATSRCLN (NDB_No );
CREATE UNIQUE INDEX DATSRCLN_PrimaryKey ON DATSRCLN (NDB_No , Nutr_no , DataSrc_ID );
CREATE UNIQUE INDEX FD_GROUP_PrimaryKey ON FD_GROUP (FdGrp_CD );
CREATE INDEX FLAV_DAT_FOOD_DESFLAV_DAT ON FLAV_DAT (NDB_No );
CREATE INDEX FLAV_DAT_NUTR_DEFFLAV_DAT ON FLAV_DAT (Nutr_no );
CREATE UNIQUE INDEX FLAV_DAT_PrimaryKey ON FLAV_DAT (NDB_No , Nutr_no );
CREATE INDEX FLAV_IND_DataSrc_ID ON FLAV_IND (DataSrc_ID );
CREATE INDEX FLAV_IND_FLAV_INDNDB_No ON FLAV_IND (NDB_No );
CREATE INDEX FLAV_IND_Num_Data_Pts ON FLAV_IND (Num_Data_Pts );
CREATE INDEX FOOD_DES_FD_GROUPFOOD_DES ON FOOD_DES (FdGrp_Cd );
CREATE UNIQUE INDEX FOOD_DES_PrimaryKey ON FOOD_DES (NDB_No );
CREATE UNIQUE INDEX NUTR_DEF_PrimaryKey ON NUTR_DEF (Nutrr_no );
