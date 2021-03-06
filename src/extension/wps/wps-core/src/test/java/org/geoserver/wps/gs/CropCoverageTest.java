package org.geoserver.wps.gs;

import java.io.InputStream;

import org.geoserver.data.test.MockData;
import org.geoserver.wps.WPSTestSupport;
import org.geotools.gce.arcgrid.ArcGridFormat;
import org.geotools.geometry.DirectPosition2D;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.opengis.coverage.grid.GridCoverage;

import com.mockrunner.mock.web.MockHttpServletResponse;
import com.vividsolutions.jts.geom.Envelope;

public class CropCoverageTest extends WPSTestSupport {
    
    @Override
    protected void populateDataDirectory(MockData dataDirectory) throws Exception {
        super.populateDataDirectory(dataDirectory);
        dataDirectory.addWcs11Coverages();
    }

    public void testCrop() throws Exception {
        String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
                + "<wps:Execute version=\"1.0.0\" service=\"WPS\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns=\"http://www.opengis.net/wps/1.0.0\" xmlns:wfs=\"http://www.opengis.net/wfs\" xmlns:wps=\"http://www.opengis.net/wps/1.0.0\" xmlns:ows=\"http://www.opengis.net/ows/1.1\" xmlns:gml=\"http://www.opengis.net/gml\" xmlns:ogc=\"http://www.opengis.net/ogc\" xmlns:wcs=\"http://www.opengis.net/wcs/1.1.1\" xmlns:xlink=\"http://www.w3.org/1999/xlink\" xsi:schemaLocation=\"http://www.opengis.net/wps/1.0.0 http://schemas.opengis.net/wps/1.0.0/wpsAll.xsd\">\n"
                + "  <ows:Identifier>gs:CropCoverage</ows:Identifier>\n"
                + "  <wps:DataInputs>\n"
                + "    <wps:Input>\n"
                + "      <ows:Identifier>coverage</ows:Identifier>\n"
                + "      <wps:Reference mimeType=\"image/tiff\" xlink:href=\"http://geoserver/wcs\" method=\"POST\">\n"
                + "        <wps:Body>\n"
                + "          <wcs:GetCoverage service=\"WCS\" version=\"1.1.1\">\n"
                + "            <ows:Identifier>" + getLayerId(MockData.TASMANIA_DEM) + "</ows:Identifier>\n"
                + "            <wcs:DomainSubset>\n"
                + "              <gml:BoundingBox crs=\"http://www.opengis.net/gml/srs/epsg.xml#4326\">\n"
                + "                <ows:LowerCorner>-180.0 -90.0</ows:LowerCorner>\n"
                + "                <ows:UpperCorner>180.0 90.0</ows:UpperCorner>\n"
                + "              </gml:BoundingBox>\n"
                + "            </wcs:DomainSubset>\n"
                + "            <wcs:Output format=\"image/tiff\"/>\n"
                + "          </wcs:GetCoverage>\n"
                + "        </wps:Body>\n"
                + "      </wps:Reference>\n"
                + "    </wps:Input>\n"
                + "    <wps:Input>\n"
                + "      <ows:Identifier>cropShape</ows:Identifier>\n"
                + "      <wps:Data>\n"
                + "        <wps:ComplexData mimeType=\"application/wkt\"><![CDATA[POLYGON((145.5 -41.9, 145.5 -42.1, 145.6 -42, 145.5 -41.9))]]></wps:ComplexData>\n"
                + "      </wps:Data>\n" + "    </wps:Input>\n" + "  </wps:DataInputs>\n"
                + "  <wps:ResponseForm>\n"
                + "    <wps:RawDataOutput mimeType=\"application/arcgrid\">\n"
                + "      <ows:Identifier>result</ows:Identifier>\n" + "    </wps:RawDataOutput>\n"
                + "  </wps:ResponseForm>\n" + "</wps:Execute>\n" + "\n" + "";

        MockHttpServletResponse response = postAsServletResponse(root(), xml);
        InputStream is = getBinaryInputStream(response);
        
        ArcGridFormat format = new ArcGridFormat();
        GridCoverage gc = format.getReader(is).read(null);
        
        assertTrue(new Envelope(-145.4, 145.6, -41.8, -42.1).contains(new ReferencedEnvelope(gc.getEnvelope())));
        
        double[] valueInside = (double[]) gc.evaluate(new DirectPosition2D(145.55, -42));
        assertEquals(615.0, valueInside[0]);
        double[] valueOutside = (double[]) gc.evaluate(new DirectPosition2D(145.57, -41.9));
        // this should really be NaN...
        assertEquals(0.0, valueOutside[0]);

    }
}
