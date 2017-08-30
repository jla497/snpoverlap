#!/bin/bash
#wrapper script for running the chromosome to snp mapping and overlap functions

while [[ $# -gt 1 ]]
	do
	key="$1"
	
	case $key in
		-i1|--input1)
		INPUT1="$2"
		shift
		;;
		-i2|--input2) 
		INPUT2="$2"
		;;*)
		
		;;
	esac
	shift
done



echo INPUT1 ="${INPUT1}"

echo INPUT2 ="${INPUT2}"

mkdir ${PWD}/output

echo mapping chromosome ranges to snps...

java -jar target/chromosome_to_snp-1.0.jar "${INPUT1}" "${INPUT2}"

cp snps1.rs snps2.rs ${PWD}/output

mv snps1.rs snps2.rs ${PWD}/snpLift

cd ${PWD}/snpLift

rm *.md5

rm *.gz

 wget --user=anonymous --password='jaehonglee86@gmail.com' ftp://ftp.ncbi.nlm.nih.gov/snp/organisms/human_9606_b150_GRCh38p7/database/data/organism_data/SNPHistory.bcp.gz 
 wget --user=anonymous --password='jaehonglee86@gmail.com' ftp://ftp.ncbi.nlm.nih.gov/snp/organisms/human_9606_b150_GRCh38p7/database/data/organism_data/SNPHistory.bcp.gz.md5 
 wget --user=anonymous --password='jaehonglee86@gmail.com' ftp://ftp.ncbi.nlm.nih.gov/snp/organisms/human_9606_b150_GRCh38p7/database/data/organism_data/RsMergeArch.bcp.gz
 wget --user=anonymous --password='jaehonglee86@gmail.com' ftp://ftp.ncbi.nlm.nih.gov/snp/organisms/human_9606_b150_GRCh38p7/database/data/organism_data/RsMergeArch.bcp.gz.md5 
 
for f in *.gz
	do

	md5Check=$(md5sum "${f}" | awk '{print $1}') 
	
	md5Ref=$(cat ${f}.md5 | sed -e 's/MD5('"$f"')=//' | tr -d '[:space:]') 

	if [ $md5Check != $md5Ref ];
	then
		echo md5sum for file $f does not match
		exit 1
	fi

done

echo running liftRsNumber.py for snps1.rs...

python LiftRsNumber.py snps1.rs > snps1_remapped.rs 

parentdir="$(dirname "$(PWD)")"

cp snps1_remapped.rs $parentdir/output

rm snps1.rs

echo running liftRsNumber.py for snps2.rs...


python LiftRsNumber.py snps2.rs > snps2_remapped.rs 

cp snps2_remapped.rs $parentdir/output

rm snps2.rs

cat snps1_remapped.rs | awk '{print $2}'| sort -o sorted_snp1.rs 

cat snps2_remapped.rs | awk '{print $2}'| sort -o sorted_snp2.rs 

rm snps1_remapped.rs snps2_remapped.rs

comm -12 sorted_snp1.rs sorted_snp2.rs > matchedSnps_remapped.rs

cp matchedSnps_remapped.rs $parentdir/output

rm *.rs


