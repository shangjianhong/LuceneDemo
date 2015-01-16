package com.search.lucene;

import java.awt.TextField;
import java.io.File;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field.Store;
import org.apache.lucene.document.StringField;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;
import org.junit.Test;

public class IndexFile {

    protected String[] ids={"1", "2"};

    protected String[] content={"Amsterdam has lost of add  cancals", "i love  add this girl"};

    protected String[] city={"Amsterdam", "Venice"};

    private Directory dir;

    /**
     * ��ʼ����ĵ�
     * @throws Exception
     */
    @Test
    public void init() throws Exception {
        String pathFile="D://common_soft";
        dir=FSDirectory.open(new File(pathFile));
        IndexWriter writer=getWriter();
        for(int i=0; i < ids.length; i++) {
            Document doc=new Document();
            doc.add(new StringField("id", ids[i], Store.YES));
           // doc.add(new TextField("content", content[i], Store.YES));
            doc.add(new StringField("city", city[i], Store.YES));
            writer.addDocument(doc);
        }
        System.out.println("init ok?");
        writer.close();
    }

    /**
     * ���IndexWriter����
     * @return
     * @throws Exception
     */
    public IndexWriter getWriter() throws Exception {
        Analyzer analyzer=new StandardAnalyzer(Version.LUCENE_40);
        IndexWriterConfig iwc=new IndexWriterConfig(Version.LUCENE_40, analyzer);
        return new IndexWriter(dir, iwc);
    }

}
