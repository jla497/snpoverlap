#!/bin/bash
#This script downloads human snp bed files from the NCBI ftp server and 
#sorts each bed files by the third column of each row (chromosome end position)

for ((var=1;var<=22;var++))
	do
		wget --user=anonymous --password='jaehonglee86@gmail.com' ftp://ftp.ncbi.nih.gov/snp/organisms/human_9606_b150_GRCh37p13/BED/bed_chr_${var}.bed.gz
		wget --user=anonymous --password='jaehonglee86@gmail.com' ftp://ftp.ncbi.nih.gov/snp/organisms/human_9606_b150_GRCh37p13/BED/bed_chr_${var}.bed.gz.md5
	done

wget --user=anonymous --password='jaehonglee86@gmail.com' ftp://ftp.ncbi.nih.gov/snp/organisms/human_9606_b150_GRCh37p13/BED/bed_chr_X.bed.gz
wget --user=anonymous --password='jaehonglee86@gmail.com' ftp://ftp.ncbi.nih.gov/snp/organisms/human_9606_b150_GRCh37p13/BED/bed_chr_X.bed.gz.md5
wget --user=anonymous --password='jaehonglee86@gmail.com' ftp://ftp.ncbi.nih.gov/snp/organisms/human_9606_b150_GRCh37p13/BED/bed_chr_Y.bed.gz
wget --user=anonymous --password='jaehonglee86@gmail.com' ftp://ftp.ncbi.nih.gov/snp/organisms/human_9606_b150_GRCh37p13/BED/bed_chr_Y.bed.gz.md5
wget --user=anonymous --password='jaehonglee86@gmail.com' ftp://ftp.ncbi.nih.gov/snp/organisms/human_9606_b150_GRCh37p13/BED/bed_chr_MT.bed.gz
wget --user=anonymous --password='jaehonglee86@gmail.com' ftp://ftp.ncbi.nih.gov/snp/organisms/human_9606_b150_GRCh37p13/BED/bed_chr_MT.bed.gz.md5
	
echo finished downloading all bed.gz files...

echo checking md5sum...

for f in *.bed.gz
	do

	md5Check=$(md5sum "${f}" | awk '{print $1}') 
	
	md5Ref=$(cat ${f}.md5 | sed -e 's/MD5('"$f"')=//' | tr -d '[:space:]') 

	if [ $md5Check != $md5Ref ];
	then
		echo md5sum for file $f does not match
		exit 1
	fi

done

rm *.md5

echo unzipping all bed.gz files...

gunzip *.bed.gz 

echo finished unzipping all .gz files...

for f in *.bed
	do
		sort -nk3 ${f} -o r_sorted_${f}
		rm ${f}
		echo sorted ${f}
	done

mkdir ${PWD}/bedfiles
mv *.bed ${PWD}/bedfiles
echo done