/*
 * Copyright (c) 2011, Regents of the University of Colorado 
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 * Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer. 
 * Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the documentation and/or other materials provided with the distribution. 
 * Neither the name of the University of Colorado at Boulder nor the names of its contributors may be used to endorse or promote products derived from this software without specific prior written permission. 
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE. 
 */
package org.cleartk.syntax.constituent.type;

import java.util.Arrays;

import org.apache.uima.analysis_engine.AnalysisEngine;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.tcas.Annotation;
import org.cleartk.syntax.SyntaxTestBase;
import org.cleartk.syntax.constituent.TreebankConstants;
import org.cleartk.syntax.constituent.TreebankGoldAnnotator;
import org.cleartk.syntax.constituent.type.TreebankNodeUtil.TreebankNodePath;
import org.junit.Assert;
import org.junit.Test;
import org.uimafit.factory.AnalysisEngineFactory;
import org.uimafit.util.JCasUtil;

/**
 * <br>
 * Copyright (c) 2011, Regents of the University of Colorado <br>
 * All rights reserved.
 * 
 * @author Steven Bethard
 */
public class TreebankNodeUtilTest extends SyntaxTestBase {

  @Test
  public void testSelectMatchingLeaf() throws Exception {
    this.jCas.setDocumentText("The cat chased the mouse.");

    JCas tbView = this.jCas.createView(TreebankConstants.TREEBANK_VIEW);
    tbView.setDocumentText("(S (NP (DT The) (NN cat)) (VP (VBD chased) (NP (DT the) (NN mouse))) (. .))");

    AnalysisEngine treeAnnotator = AnalysisEngineFactory.createPrimitive(
        TreebankGoldAnnotator.class,
        this.typeSystemDescription);
    treeAnnotator.process(this.jCas);

    TreebankNode node;
    node = TreebankNodeUtil.selectMatchingLeaf(this.jCas, this.newSpan(0, 3));
    Assert.assertEquals("The", node.getCoveredText());
    Assert.assertEquals("DT", node.getNodeType());
    Assert.assertTrue(node.getLeaf());

    node = TreebankNodeUtil.selectMatchingLeaf(this.jCas, this.newSpan(8, 14));
    Assert.assertEquals("chased", node.getCoveredText());
    Assert.assertEquals("VBD", node.getNodeType());
    Assert.assertTrue(node.getLeaf());

    node = TreebankNodeUtil.selectMatchingLeaf(this.jCas, this.newSpan(0, 4));
    Assert.assertNull(node);

    int end = this.jCas.getDocumentText().length();
    node = TreebankNodeUtil.selectMatchingLeaf(this.jCas, this.newSpan(0, end));
    Assert.assertNull(node);
  }

  @Test
  public void testSelectHighestCoveredTreebankNode() throws Exception {
    this.jCas.setDocumentText("The cat chased mice.");

    JCas tbView = this.jCas.createView(TreebankConstants.TREEBANK_VIEW);
    tbView.setDocumentText("(S (NP (DT The) (NN cat)) (VP (VBD chased) (NP (NNS mice))) (. .))");

    AnalysisEngine treeAnnotator = AnalysisEngineFactory.createPrimitive(
        TreebankGoldAnnotator.class,
        this.typeSystemDescription);
    treeAnnotator.process(this.jCas);

    TreebankNode node;
    node = TreebankNodeUtil.selectHighestCoveredTreebankNode(this.jCas, this.newSpan(0, 3));
    Assert.assertEquals("The", node.getCoveredText());
    Assert.assertEquals("DT", node.getNodeType());
    Assert.assertTrue(node.getLeaf());

    node = TreebankNodeUtil.selectHighestCoveredTreebankNode(this.jCas, this.newSpan(15, 19));
    Assert.assertEquals("mice", node.getCoveredText());
    Assert.assertEquals("NP", node.getNodeType());
    Assert.assertFalse(node.getLeaf());

    node = TreebankNodeUtil.selectHighestCoveredTreebankNode(this.jCas, this.newSpan(8, 19));
    Assert.assertEquals("chased mice", node.getCoveredText());
    Assert.assertEquals("VP", node.getNodeType());
    Assert.assertFalse(node.getLeaf());

    int end = this.jCas.getDocumentText().length();
    node = TreebankNodeUtil.selectHighestCoveredTreebankNode(this.jCas, this.newSpan(0, end));
    Assert.assertEquals("The cat chased mice.", node.getCoveredText());
    Assert.assertEquals("S", node.getNodeType());
    Assert.assertFalse(node.getLeaf());

    node = TreebankNodeUtil.selectMatchingLeaf(this.jCas, this.newSpan(0, 4));
    Assert.assertNull(node);

    node = TreebankNodeUtil.selectMatchingLeaf(this.jCas, this.newSpan(0, end - 1));
    Assert.assertNull(node);
  }

  @Test
  public void testGetDepth() throws Exception {
    this.jCas.setDocumentText("The cat chased mice.");

    JCas tbView = this.jCas.createView(TreebankConstants.TREEBANK_VIEW);
    tbView.setDocumentText("(S (NP (DT The) (NN cat)) (VP (VBD chased) (NP (NNS mice))) (. .))");

    AnalysisEngine treeAnnotator = AnalysisEngineFactory.createPrimitive(
        TreebankGoldAnnotator.class,
        this.typeSystemDescription);
    treeAnnotator.process(this.jCas);

    for (TreebankNode node : JCasUtil.select(this.jCas, TreebankNode.class)) {
      String text = node.getCoveredText();
      int depth = TreebankNodeUtil.getDepth(node);
      if (text.equals("The")) {
        Assert.assertEquals(2, depth);
      } else if (text.equals("cat")) {
        Assert.assertEquals(2, depth);
      } else if (text.equals("The cat")) {
        Assert.assertEquals(1, depth);
      } else if (text.equals("chased")) {
        Assert.assertEquals(2, depth);
      } else if (text.equals("mice")) {
        if (node.getLeaf()) {
          Assert.assertEquals(3, depth);
        } else {
          Assert.assertEquals(2, depth);
        }
      } else if (text.equals("chased mice")) {
        Assert.assertEquals(1, depth);
      } else if (text.equals(".")) {
        Assert.assertEquals(1, depth);
      } else if (text.equals("The cat chased mice.")) {
        Assert.assertEquals(0, depth);
      } else {
        Assert.fail("Unexpected node: " + node);
      }
    }
  }

  @Test
  public void testGetPathToRoot() throws Exception {
    this.jCas.setDocumentText("The cat chased mice.");

    JCas tbView = this.jCas.createView(TreebankConstants.TREEBANK_VIEW);
    tbView.setDocumentText("(S (NP (DT The) (NN cat)) (VP (VBD chased) (NP (NNS mice))) (. .))");

    AnalysisEngine treeAnnotator = AnalysisEngineFactory.createPrimitive(
        TreebankGoldAnnotator.class,
        this.typeSystemDescription);
    treeAnnotator.process(this.jCas);

    TopTreebankNode root = JCasUtil.selectSingle(this.jCas, TopTreebankNode.class);
    Assert.assertEquals(Arrays.asList(root), TreebankNodeUtil.getPathToRoot(root));

    TreebankNode np = root.getChildren(0);
    TreebankNode vp = root.getChildren(1);
    TreebankNode period = root.getChildren(2);
    Assert.assertEquals(Arrays.asList(np, root), TreebankNodeUtil.getPathToRoot(np));
    Assert.assertEquals(Arrays.asList(vp, root), TreebankNodeUtil.getPathToRoot(vp));
    Assert.assertEquals(Arrays.asList(period, root), TreebankNodeUtil.getPathToRoot(period));

    TreebankNode the = np.getChildren(0);
    TreebankNode cat = np.getChildren(1);
    Assert.assertEquals(Arrays.asList(the, np, root), TreebankNodeUtil.getPathToRoot(the));
    Assert.assertEquals(Arrays.asList(cat, np, root), TreebankNodeUtil.getPathToRoot(cat));

    TreebankNode chased = vp.getChildren(0);
    TreebankNode np2 = vp.getChildren(1);
    Assert.assertEquals(Arrays.asList(chased, vp, root), TreebankNodeUtil.getPathToRoot(chased));
    Assert.assertEquals(Arrays.asList(np2, vp, root), TreebankNodeUtil.getPathToRoot(np2));

    TreebankNode mice = np2.getChildren(0);
    Assert.assertEquals(Arrays.asList(mice, np2, vp, root), TreebankNodeUtil.getPathToRoot(mice));
  }

  @Test
  public void testGetPath() throws Exception {
    this.jCas.setDocumentText("The cat chased mice.");

    JCas tbView = this.jCas.createView(TreebankConstants.TREEBANK_VIEW);
    tbView.setDocumentText("(S (NP (DT The) (NN cat)) (VP (VBD chased) (NP (NNS mice))) (. .))");

    AnalysisEngine treeAnnotator = AnalysisEngineFactory.createPrimitive(
        TreebankGoldAnnotator.class,
        this.typeSystemDescription);
    treeAnnotator.process(this.jCas);

    TopTreebankNode root = JCasUtil.selectSingle(this.jCas, TopTreebankNode.class);
    TreebankNode np = root.getChildren(0);
    TreebankNode vp = root.getChildren(1);
    TreebankNode period = root.getChildren(2);
    TreebankNode the = np.getChildren(0);
    TreebankNode cat = np.getChildren(1);
    TreebankNode chased = vp.getChildren(0);
    TreebankNode np2 = vp.getChildren(1);
    TreebankNode mice = np2.getChildren(0);

    TreebankNodePath path = TreebankNodeUtil.getPath(cat, mice);
    Assert.assertEquals(root, path.getCommonAncestor());
    Assert.assertEquals(Arrays.asList(cat, np), path.getSourceToAncestorPath());
    Assert.assertEquals(Arrays.asList(mice, np2, vp), path.getTargetToAncestorPath());

    path = TreebankNodeUtil.getPath(mice, chased);
    Assert.assertEquals(vp, path.getCommonAncestor());
    Assert.assertEquals(Arrays.asList(mice, np2), path.getSourceToAncestorPath());
    Assert.assertEquals(Arrays.asList(chased), path.getTargetToAncestorPath());

    path = TreebankNodeUtil.getPath(the, the);
    Assert.assertEquals(the, path.getCommonAncestor());
    Assert.assertEquals(Arrays.asList(), path.getSourceToAncestorPath());
    Assert.assertEquals(Arrays.asList(), path.getTargetToAncestorPath());

    path = TreebankNodeUtil.getPath(root, period);
    Assert.assertEquals(root, path.getCommonAncestor());
    Assert.assertEquals(Arrays.asList(), path.getSourceToAncestorPath());
    Assert.assertEquals(Arrays.asList(period), path.getTargetToAncestorPath());
  }

  @Test
  public void testToTreebankString() throws Exception {
    for (String parseText : Arrays.asList(
        "(S (NP (DT The) (NN cat)) (VP (VBD chased) (NP (NNS mice))) (. .))",
        "(S (NP (DT The) (NN skunk)) (VP (VBD thought) (S (NP (DT the) (NN stump)) (VP (VBD stunk)))) (. .))")) {
      this.jCas.reset();
      JCas tbView = this.jCas.createView(TreebankConstants.TREEBANK_VIEW);
      tbView.setDocumentText(parseText);

      AnalysisEngine treeAnnotator = AnalysisEngineFactory.createPrimitive(
          TreebankGoldAnnotator.class,
          this.typeSystemDescription);
      treeAnnotator.process(this.jCas);

      TopTreebankNode root = JCasUtil.selectSingle(this.jCas, TopTreebankNode.class);
      Assert.assertEquals(parseText, TreebankNodeUtil.toTreebankString(root));
    }
  }

  private Annotation newSpan(int begin, int end) {
    return new Annotation(this.jCas, begin, end);
  }
}