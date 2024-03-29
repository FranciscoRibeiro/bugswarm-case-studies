/*
 * This file is part of the OWL API.
 *
 * The contents of this file are subject to the LGPL License, Version 3.0.
 *
 * Copyright (C) 2011, The University of Manchester
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see http://www.gnu.org/licenses/.
 *
 *
 * Alternatively, the contents of this file may be used under the terms of the Apache License, Version 2.0
 * in which case, the provisions of the Apache License Version 2.0 are applicable instead of those above.
 *
 * Copyright 2011, University of Manchester
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.coode.owlapi.obo12.parser;

import static org.semanticweb.owlapi.util.OWLAPIPreconditions.verifyNotNull;

import java.io.Reader;
import java.util.List;

import org.semanticweb.owlapi.io.AbstractOWLParser;
import org.semanticweb.owlapi.io.OWLParserParameters;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLDocumentFormat;
import org.semanticweb.owlapi.model.OWLDocumentFormatFactory;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OntologyConfigurator;

/**
 * Author: Matthew Horridge<br>
 * The University Of Manchester<br>
 * Bio-Health Informatics Group<br>
 * Date: 10-Jan-2007<br>
 * <br>
 */
class OWLOBO12Parser extends AbstractOWLParser {

    @Override
    public OWLDocumentFormat parse(Reader r, OWLParserParameters p) {
        return parse(p.getOntology(), p.getConfig(), p.getDocumentIRI(), new StreamProvider(r));
    }

    @Override
    public OWLDocumentFormat parse(String s, OWLParserParameters p) {
        return parse(p.getOntology(), p.getConfig(), p.getDocumentIRI(), new StringProvider(s));
    }

    protected OWLDocumentFormat parse(OWLOntology o, OntologyConfigurator config, IRI documentIRI,
        Provider provider) {
        RawFrameHandler rawFrameHandler = new RawFrameHandler();
        OBOConsumer oboConsumer = new OBOConsumer(o, config, documentIRI);
        OBOParser parser = new OBOParser(provider);
        parser.setHandler(rawFrameHandler);
        parser.parse();
        parseFrames(rawFrameHandler, oboConsumer);
        OBO12DocumentFormat format = new OBO12DocumentFormat();
        format.setIDSpaceManager(oboConsumer.getIdSpaceManager());
        return format;
    }

    private static void parseFrames(RawFrameHandler rawFrameHandler, OBOConsumer oboConsumer) {
        parseHeaderFrame(rawFrameHandler, oboConsumer);
        parseFrames(oboConsumer, rawFrameHandler.getTypeDefFrames());
        parseFrames(oboConsumer, rawFrameHandler.getNonTypeDefFrames());
    }

    private static void parseHeaderFrame(RawFrameHandler rawFrameHandler, OBOConsumer consumer) {
        consumer.startHeader();
        parseFrameTagValuePairs(consumer, verifyNotNull(rawFrameHandler.getHeaderFrame()));
        consumer.endHeader();
    }

    private static void parseFrames(OBOConsumer oboConsumer, List<OBOFrame> frames) {
        for (OBOFrame frame : frames) {
            parseFrame(oboConsumer, frame);
        }
    }

    private static void parseFrame(OBOConsumer oboConsumer, OBOFrame frame) {
        oboConsumer.startFrame(frame.getFrameType());
        parseFrameTagValuePairs(oboConsumer, frame);
        oboConsumer.endFrame();
    }

    private static void parseFrameTagValuePairs(OBOConsumer oboConsumer, OBOFrame frame) {
        for (OBOTagValuePair tagValuePair : frame.getTagValuePairs()) {
            oboConsumer.handleTagValue(tagValuePair.getTagName(), tagValuePair.getValue(),
                tagValuePair.getQualifier(), tagValuePair.getComment());
        }
    }

    @Override
    public OWLDocumentFormatFactory getSupportedFormat() {
        return new OBO12DocumentFormatFactory();
    }
}
