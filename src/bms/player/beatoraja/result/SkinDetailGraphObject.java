package bms.player.beatoraja.result;

import bms.model.BMSModel;
import bms.model.LongNote;
import bms.model.Note;
import bms.model.TimeLine;
import bms.player.beatoraja.MainState;
import bms.player.beatoraja.skin.SkinObject;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Rectangle;
import org.lwjgl.opengl.GL11;

/**
 * Created by exch on 2016/11/18.
 */
public class SkinDetailGraphObject extends SkinObject {

    private ShapeRenderer shape = new ShapeRenderer();

    private int[][] data;
    private int[][] fdata;

    private static final String[] JGRAPH = { "555555", "0088ff", "00ff88", "ffff00", "ff8800", "ff0000" };
    private static final String[] FGRAPH = { "555555", "44ff44", "0088ff", "0066cc", "004488", "002244", "ff8800",
            "cc6600", "884400", "442200" };

    private int max = 20;

    public void draw(SpriteBatch sprite, long time, MainState state) {
        if(data == null) {
            BMSModel model = state.getMainController().getPlayerResource().getBMSModel();
            data = new int[model.getLastTime() / 1000 + 1][6];
            fdata = new int[model.getLastTime() / 1000 + 1][10];
            int pos = 0;
            int count = 0;
            max = 20;
            for (TimeLine tl : model.getAllTimeLines()) {
                if (tl.getTime() / 1000 != pos) {
                    if (max < count) {
                        max = (count / 10) * 10 + 10;
                    }
                    pos = tl.getTime() / 1000;
                    count = 0;
                }
                for (int i = 0; i < 18; i++) {
                    Note n = tl.getNote(i);
                    if (n != null
                            && !(model.getLntype() == BMSModel.LNTYPE_LONGNOTE && n instanceof LongNote && ((LongNote) n)
                            .getEndnote().getSection() == tl.getSection())) {
                        int st = n.getState();
                        int t = n.getTime();
                        if (n instanceof LongNote && ((LongNote) n).getEndnote().getSection() == tl.getSection()) {
                            st = ((LongNote) n).getEndnote().getState();
                            t = ((LongNote) n).getEndnote().getTime();
                            // if(state == 0) {
                            // System.out.println("終端未処理:"+tl.getTime());
                            // }
                        }
                        data[tl.getTime() / 1000][st]++;
                        count++;
                        if (st <= 1) {
                            fdata[tl.getTime() / 1000][st]++;
                        } else {
                            fdata[tl.getTime() / 1000][t >= 0 ? st : st + 4]++;
                        }
                    }
                }
            }
        }
        sprite.end();
        if ((time / 5000) % 2 == 0) {
            drawGraph(getDestination(time,state), data, JGRAPH);
            sprite.begin();
//            titlefont.setColor(Color.GREEN);
//            titlefont.draw(sprite, "JUDGE DETAIL", 500, 700);
        } else {
            drawGraph(getDestination(time,state), fdata, FGRAPH);
            sprite.begin();
//            titlefont.setColor(Color.CYAN);
//            titlefont.draw(sprite, "EARLY/LATE", 500, 700);
        }
    }

    private void drawGraph(Rectangle judge, int[][] data, String[] GRAPH) {
        if (judge == null) {
            return;
        }
        Gdx.gl.glEnable(GL11.GL_BLEND);
        Gdx.gl.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        shape.begin(ShapeRenderer.ShapeType.Filled);
        shape.setColor(0, 0, 0, 0.8f);
        shape.rect(judge.x, judge.y, judge.width, judge.height);
        shape.end();
        Gdx.gl.glDisable(GL11.GL_BLEND);

        shape.begin(ShapeRenderer.ShapeType.Line);
        for (int i = 10; i < max; i += 10) {
            shape.setColor(Color.valueOf("444444"));
            shape.line(judge.x, judge.y + judge.height * i / max, judge.x + judge.width, judge.y + judge.height * i
                    / max);
        }
        shape.end();
        for (int i = 0; i < data.length; i++) {
            // BPM変化地点描画
            int[] n = data[i];
            // x軸補助線描画
            if (i % 60 == 0) {
                shape.begin(ShapeRenderer.ShapeType.Line);
                shape.setColor(Color.valueOf("444444"));
                shape.line(judge.x + i * judge.width / data.length, judge.y, judge.x + i * judge.width / data.length,
                        judge.y + judge.height);
                shape.end();
            } else if (i % 10 == 0) {
                shape.begin(ShapeRenderer.ShapeType.Line);
                shape.setColor(Color.valueOf("222222"));
                shape.line(judge.x + i * judge.width / data.length, judge.y, judge.x + i * judge.width / data.length,
                        judge.y + judge.height);
                shape.end();
            }

            shape.begin(ShapeRenderer.ShapeType.Filled);
            for (int j = 0, k = n[0], index = 0; index < GRAPH.length;) {
                if (k > 0) {
                    k--;
                    shape.setColor(Color.valueOf(GRAPH[index]));
                    shape.rect(judge.x + judge.width * i / data.length, judge.y + j * (judge.height / max), judge.width
                            / data.length - 1, (judge.height / max) - 1);
                    j++;
                } else {
                    index++;
                    if (index == GRAPH.length) {
                        break;
                    }
                    k = n[index];
                }
            }
            shape.end();
        }
    }

    @Override
    public void dispose() {
        shape.dispose();
    }
}
