/*
 * $Id$
 *
 * Authors:
 *      Jeff Buchbinder <jeff@freemedsoftware.org>
 *      Example code from Bouncy Castle -- thanks guys!
 *
 * REMITT Electronic Medical Information Translation and Transmission
 * Copyright (C) 1999-2012 FreeMED Software Foundation
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
 */

package org.remitt.server;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.NoSuchProviderException;
import java.security.SecureRandom;
import java.util.Iterator;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.bouncycastle.bcpg.ArmoredOutputStream;
import org.bouncycastle.openpgp.PGPCompressedData;
import org.bouncycastle.openpgp.PGPCompressedDataGenerator;
import org.bouncycastle.openpgp.PGPEncryptedData;
import org.bouncycastle.openpgp.PGPEncryptedDataGenerator;
import org.bouncycastle.openpgp.PGPEncryptedDataList;
import org.bouncycastle.openpgp.PGPException;
import org.bouncycastle.openpgp.PGPLiteralData;
import org.bouncycastle.openpgp.PGPObjectFactory;
import org.bouncycastle.openpgp.PGPOnePassSignatureList;
import org.bouncycastle.openpgp.PGPPrivateKey;
import org.bouncycastle.openpgp.PGPPublicKey;
import org.bouncycastle.openpgp.PGPPublicKeyEncryptedData;
import org.bouncycastle.openpgp.PGPPublicKeyRing;
import org.bouncycastle.openpgp.PGPPublicKeyRingCollection;
import org.bouncycastle.openpgp.PGPSecretKey;
import org.bouncycastle.openpgp.PGPSecretKeyRingCollection;
import org.bouncycastle.openpgp.PGPUtil;

public class PGPProvider {

	static final Logger log = Logger.getLogger(PGPProvider.class);

	/**
	 * 
	 * @param keyname
	 * @return
	 * @throws IOException
	 */
	public static InputStream getKeyFromFile(String keyname) throws IOException {
		return FileUtils.openInputStream(new File(Configuration
				.getServletContext().getServletContext().getRealPath(
						"/WEB-INF/keys/" + keyname + ".asc")));
	}

	/**
	 * 
	 * @param pgpSec
	 * @param keyID
	 * @param pass
	 * @return
	 * @throws PGPException
	 * @throws NoSuchProviderException
	 */
	private static PGPPrivateKey findSecretKey(
			PGPSecretKeyRingCollection pgpSec, long keyID, char[] pass)
			throws PGPException, NoSuchProviderException {
		PGPSecretKey pgpSecKey = pgpSec.getSecretKey(keyID);
		return pgpSecKey == null ? null : pgpSecKey.extractPrivateKey(pass,
				"BC");
	}

	/**
	 * 
	 * @param keyname
	 * @return
	 * @throws IOException
	 * @throws PGPException
	 */
	@SuppressWarnings("unchecked")
	private static PGPPublicKey readPublicKey(String keyname)
			throws IOException, PGPException {
		InputStream in = PGPUtil.getDecoderStream(getKeyFromFile(keyname));
		PGPPublicKeyRingCollection pgpPub = new PGPPublicKeyRingCollection(in);
		Iterator<PGPPublicKeyRing> rIt = pgpPub.getKeyRings();
		while (rIt.hasNext()) {
			PGPPublicKeyRing kRing = rIt.next();
			Iterator<PGPPublicKey> kIt = kRing.getPublicKeys();
			while (kIt.hasNext()) {
				PGPPublicKey k = kIt.next();
				if (k.isEncryptionKey()) {
					return k;
				}
			}
		}
		throw new IllegalArgumentException("No key found in ring");
	}

	/**
	 * 
	 * @param in
	 * @param keyIn
	 * @param password
	 * @return
	 * @throws IOException
	 * @throws NoSuchProviderException
	 */
	public static byte[] decryptMessage(InputStream in, InputStream keyIn,
			char[] password) throws IOException, NoSuchProviderException {
		in = PGPUtil.getDecoderStream(in);
		try {
			PGPObjectFactory pgpF = new PGPObjectFactory(in);
			PGPEncryptedDataList enc = null;
			Object o = pgpF.nextObject();
			if (o instanceof PGPEncryptedDataList) {
				enc = (PGPEncryptedDataList) o;
			} else {
				enc = (PGPEncryptedDataList) pgpF.nextObject();
			}

			Iterator it = enc.getEncryptedDataObjects();
			PGPPrivateKey sKey = null;
			PGPPublicKeyEncryptedData pbe = null;
			PGPSecretKeyRingCollection pgpSec = new PGPSecretKeyRingCollection(
					PGPUtil.getDecoderStream(keyIn));

			while (sKey == null && it.hasNext()) {
				pbe = (PGPPublicKeyEncryptedData) it.next();
				sKey = findSecretKey(pgpSec, pbe.getKeyID(), password);
			}

			if (sKey == null) {
				throw new IllegalArgumentException(
						"Secret key for message not found.");
			}

			InputStream clear = pbe.getDataStream(sKey, "BC");
			PGPObjectFactory plainFact = new PGPObjectFactory(clear);
			Object message = plainFact.nextObject();

			if (message instanceof PGPCompressedData) {
				PGPCompressedData cData = (PGPCompressedData) message;
				PGPObjectFactory pgpFact = new PGPObjectFactory(cData
						.getDataStream());

				message = pgpFact.nextObject();
			}

			if (message instanceof PGPLiteralData) {
				PGPLiteralData ld = (PGPLiteralData) message;
				ByteArrayOutputStream fOut = new ByteArrayOutputStream();

				InputStream unc = ld.getInputStream();
				int ch;

				while ((ch = unc.read()) >= 0) {
					fOut.write(ch);
				}

				if (pbe.isIntegrityProtected()) {
					if (!pbe.verify()) {
						log.error("Message failed integrity check");
					} else {
						log.info("Message integrity check passed");
					}
				} else {
					log.trace("No message integrity check");
				}

				fOut.flush();
				fOut.close();
				return fOut.toByteArray();
			} else if (message instanceof PGPOnePassSignatureList) {
				throw new PGPException(
						"Encrypted message contains a signed message - not literal data.");
			} else {
				throw new PGPException(
						"Message is not a simple encrypted file - type unknown.");
			}

		} catch (PGPException ex) {
			log.error(ex);
		}

		return null;
	}

	/**
	 * 
	 * @param data
	 * @param encKeyName
	 * @return
	 * @throws IOException
	 * @throws NoSuchProviderException
	 * @throws PGPException
	 */
	public static byte[] encryptMessage(byte[] data, String encKeyName)
			throws IOException, NoSuchProviderException, PGPException {
		return encryptMessage(data, readPublicKey(encKeyName));
	}

	/**
	 * 
	 * @param data
	 * @param encKey
	 * @return
	 * @throws IOException
	 * @throws NoSuchProviderException
	 */
	public static byte[] encryptMessage(byte[] data, PGPPublicKey encKey)
			throws IOException, NoSuchProviderException, PGPException {
		boolean armor = true;
		boolean withIntegrityCheck = true;
		byte[] ret = null;

		OutputStream out = new ByteArrayOutputStream();

		if (armor) {
			out = new ArmoredOutputStream(out);
		}

		File tempfile = null;

		try {
			tempfile = File.createTempFile("pgp", null);
			FileUtils.writeByteArrayToFile(tempfile, data);

			ByteArrayOutputStream bOut = new ByteArrayOutputStream();

			PGPCompressedDataGenerator comData = new PGPCompressedDataGenerator(
					PGPCompressedData.ZIP);
			PGPUtil.writeFileToLiteralData(comData.open(bOut),
					PGPLiteralData.BINARY, tempfile);
			comData.close();

			PGPEncryptedDataGenerator cPk = new PGPEncryptedDataGenerator(
					PGPEncryptedData.CAST5, withIntegrityCheck,
					new SecureRandom(), "BC");
			cPk.addMethod(encKey);

			ret = bOut.toByteArray();
		} catch (PGPException e) {
			log.error(e);
			if (e.getUnderlyingException() != null) {
				log.error(e.getUnderlyingException());
			}
			// Clean up
			if (tempfile != null) {
				tempfile.delete();
			}
			throw e;
		} finally {
			// Clean up
			if (tempfile != null) {
				tempfile.delete();
			}
		}
		return ret;
	}

}
