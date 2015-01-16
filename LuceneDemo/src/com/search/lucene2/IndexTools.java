package com.search.lucene2;
import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.util.Date;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field.Store;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.queryparser.classic.MultiFieldQueryParser;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.TopScoreDocCollector;
import org.apache.lucene.search.highlight.Highlighter;
import org.apache.lucene.search.highlight.InvalidTokenOffsetsException;
import org.apache.lucene.search.highlight.QueryScorer;
import org.apache.lucene.search.highlight.SimpleHTMLFormatter;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;
import org.wltea.analyzer.lucene.IKAnalyzer;

public class IndexTools {
    /**
     * ���indexwriter����
     * 
     * @param dir
     * @return
     * @throws IOException
     * @throws Exception
     */
    private IndexWriter getIndexWriter(Directory dir, Analyzer analyzer) throws IOException {
        IndexWriterConfig iwc = new IndexWriterConfig(Version.LUCENE_40, analyzer);
        return new IndexWriter(dir, iwc);
    }
    
    /**
     * �ر�indexwriter����
     * 
     * @throws IOException
     * 
     * @throws Exception
     */
    private void closeWriter(IndexWriter indexWriter) throws IOException {
        if (indexWriter != null) {
            indexWriter.close();
        }
    }
    
    /**
     * ��������
     * 
     * @throws InvalidTokenOffsetsException
     */
    public void createIndex() throws InvalidTokenOffsetsException {
        String indexPath = "D://luceneindex"; // ���������ļ���Ŀ¼
        // Ĭ��IKAnalyzer()-false:ʵ����ϸ�����з��㷨,true:�ִ������������з�
        Analyzer analyzer = new IKAnalyzer(true);
        IndexWriter indexWriter = null;
        Directory directory = null;
        try {
            directory = FSDirectory.open(new File(indexPath));
            indexWriter = getIndexWriter(directory, analyzer);
        } catch (Exception e) {
            System.out.println("�������쳣��");
        }
        // �������
        try {
            Document document = new Document();
            document.add(new TextField("filename", "����:���", Store.YES));
            document.add(new TextField("content", "���ݣ�����һ������Ա", Store.YES));
            indexWriter.addDocument(document);
            Document document1 = new Document();
            document1.add(new TextField("filename", "����:�յ�", Store.YES));
            document1.add(new TextField("content", "���ݣ��Ҳ���ֻ�ǳ�XX��Ա", Store.YES));
            indexWriter.addDocument(document1);
            indexWriter.commit();
        } catch (IOException e1) {
            System.out.println("���������쳣��");
        }
        try {
            closeWriter(indexWriter);
        } catch (Exception e) {
            System.out.println("�����ر��쳣��");
        }
    }
    
    /**
     * ����
     * 
     * @throws ParseException
     * @throws IOException
     * @throws InvalidTokenOffsetsException
     */
    @SuppressWarnings({ "deprecation", "resource" })
    public void searchIndex() throws ParseException, IOException, InvalidTokenOffsetsException {
        String indexPath = "D://luceneindex"; // ���������ļ���Ŀ¼
        // Ĭ��IKAnalyzer()-false:ʵ����ϸ�����з��㷨,true:�ִ������������з�
        Analyzer analyzer = new IKAnalyzer(true);
        Directory directory = null;
        try {
            directory = FSDirectory.open(new File(indexPath));
        } catch (Exception e) {
            System.out.println("�������쳣��");
        }
        IndexReader ireader = null;
        IndexSearcher isearcher = null;
        try {
            ireader = IndexReader.open(directory);
        } catch (IOException e) {
            System.out.println("�������ļ���");
        }
        isearcher = new IndexSearcher(ireader);
        String keyword = "��XX��Ա";
        // ʹ��QueryParser��ѯ����������Query����
        // eg:�����ֶβ�ѯ
        // String fieldName = "content";
        // QueryParser qp = new QueryParser(Version.LUCENE_40, fieldName, analyzer);
        String[] fields = { "filename", "content" };
        QueryParser qp = new MultiFieldQueryParser(Version.LUCENE_40, fields, analyzer);
        qp.setDefaultOperator(QueryParser.AND_OPERATOR);
        Query query = qp.parse(keyword);
        // �������ƶ���ߵ�5����¼
        TopDocs topDocs = isearcher.search(query, 25);
        System.out.println("���У�" + topDocs.totalHits);
        // ������
        ScoreDoc[] scoreDocs = topDocs.scoreDocs;
        for (int i = 0; i < topDocs.totalHits; i++) {
            Document targetDoc = isearcher.doc(scoreDocs[i].doc);
            System.out.println("���ݣ�" + targetDoc.toString());
        }
        // ��ҳ��������ʾ
        higherIndex(analyzer, isearcher, query, topDocs);
        /* IndexWriter indexWriter = null;
        try {
            indexWriter = getIndexWriter(directory, analyzer);
            indexWriter.deleteAll(); 
        } catch (Exception e) {
            System.out.println("�������쳣��");
        }*/
    }
    
    public static void main(String[] args) {
    	System.out.println(new Date());
        IndexTools tool = new IndexTools();
        try {
        	//tool.createIndex();
            tool.searchIndex();
            System.out.println(new Date());
        } catch (ParseException e) {
            System.out.println("��������");
        } catch (IOException e) {
            System.out.println("��ȡ�ļ�������");
        } catch (InvalidTokenOffsetsException e) {
            System.out.println("��ѯʧ��");
        }
    }
    
    /**
     * ��ҳ��������ʾ
     * 
     * @param analyzer
     * @param isearcher
     * @param query
     * @param topDocs
     * @throws IOException
     * @throws InvalidTokenOffsetsException
     */
    public void higherIndex(Analyzer analyzer, IndexSearcher isearcher, Query query, TopDocs topDocs)
            throws IOException, InvalidTokenOffsetsException {
        TopScoreDocCollector results = TopScoreDocCollector.create(topDocs.totalHits, false);
        isearcher.search(query, results);
        // ��ҳȡ��ָ����doc(��ʼ����, ȡ����)
        ScoreDoc[] docs = results.topDocs(1, 2).scoreDocs;
        for (int i = 0; i < docs.length; i++) {
            Document targetDoc = isearcher.doc(docs[i].doc);
            System.out.println("���ݣ�" + targetDoc.toString());
        }
        // �ؼ��ָ�����ʾ��html��ǩ����Ҫ����lucene-highlighter-3.5.0.jar
        SimpleHTMLFormatter simpleHTMLFormatter = new SimpleHTMLFormatter("<font color='red'>", "</font>");
        Highlighter highlighter = new Highlighter(simpleHTMLFormatter, new QueryScorer(query));
        for (int i = 0; i < docs.length; i++) {
            Document doc = isearcher.doc(docs[i].doc);
            // �������Ӹ�����ʾ
            TokenStream tokenStream1 = analyzer.tokenStream("filename", new StringReader(doc.get("filename")));
            String title = highlighter.getBestFragment(tokenStream1, doc.get("filename"));
            // �������Ӹ�����ʾ
            TokenStream tokenStream2 = analyzer.tokenStream("content", new StringReader(doc.get("content")));
            String content = highlighter.getBestFragment(tokenStream2, doc.get("content"));
            System.out.println(doc.get("filename") + " : " + title + " : " + content);
        }
    }
}
