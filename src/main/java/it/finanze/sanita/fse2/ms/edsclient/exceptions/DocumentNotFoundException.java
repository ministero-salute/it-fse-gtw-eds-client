/*
 * SPDX-License-Identifier: AGPL-3.0-or-later
 * 
 * Copyright (C) 2023 Ministero della Salute
 * 
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.
 */
package it.finanze.sanita.fse2.ms.edsclient.exceptions;

/**
 * Thrown when a document is not found by the Srv Query Microservice
 * Changed to RuntimeException to allow throwing from
 * RestTemplateResponseErrorHandler
 */
public class DocumentNotFoundException extends RuntimeException {

	
	/**
     * Serial version UID
     */
	private static final long serialVersionUID = -588291874057815740L;

	
	/**
     * Complete constructor.
     *
     * @param msg	Message to be shown.
     *              It should describe what the operation was trying to accomplish.
     */
    public DocumentNotFoundException(final String msg) {
        super(msg);
        
    }
    
}
