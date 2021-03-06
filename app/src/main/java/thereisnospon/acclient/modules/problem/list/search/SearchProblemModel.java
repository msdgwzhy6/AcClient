package thereisnospon.acclient.modules.problem.list.search;

import android.text.TextUtils;
import android.util.Log;

import com.orhanobut.logger.Logger;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Response;
import thereisnospon.acclient.api.HdojApi;
import thereisnospon.acclient.data.SearchProblem;
import thereisnospon.acclient.utils.net.HttpUtil;
import thereisnospon.acclient.utils.net.request.IRequest;
import thereisnospon.acclient.utils.net.request.PostRequest;

/**
 * Created by yzr on 16/6/10.
 */
public class SearchProblemModel implements SearchProblemContact.Model {

    public static final int PRE_PAGE_NUMS=30;

    String query;
    List<SearchProblem>searchProblemList;
    int current=0;

    private boolean isLoaded(String query){
        Log.d(TAG, "isLoaded: "+query);
        return !TextUtils.isEmpty(query)&&query.equals(this.query)
                &&searchProblemList!=null&&searchProblemList.size()>0;
    }

    private boolean hasMore(){
        return searchProblemList!=null&&current<searchProblemList.size();
    }

    private List<SearchProblem>getMore(){
        Log.d(TAG, "getMore: ");
        List<SearchProblem>list=new ArrayList<>();
        for(int i=0;i<PRE_PAGE_NUMS&&current<searchProblemList.size();i++,current++){
            list.add(searchProblemList.get(i));
        }
        return list;
    }


    private void reset(String query,List<SearchProblem>list){
        Log.d(TAG, "reset: "+query+list.size());
        this.query=query;
        this.searchProblemList=list;
        this.current=0;
    }

    private static String TAG="SSEARCH";

    @Override
    public List<SearchProblem> queryProblem(String query) {
        String html=getHtml(query);
        Log.d(TAG, "quer"+query);
        Log.d(TAG, "queryProblem: "+html);
        List<SearchProblem>list=SearchProblem.Builder.parse(html);
        Logger.d("size:"+list.size());
        if(list!=null){
            reset(query,list);
            return getMore();
        }
        return null;
    }


    @Override
    public List<SearchProblem> loadMoreQuery(String query) {
        Log.d(TAG, "loadMoreQuery: "+query);
        if(isLoaded(query)){
            Log.d(TAG, "loadMoreQuery: is");
            return getMore();
        }else{
            Log.d(TAG, "loadMoreQuery: else");
            return queryProblem(query);
        }
    }


    String postPage(String query){
        try{
            Logger.d(query);
            String urlQuery=URLEncoder.encode(query,"gb2312");
            Log.e("SSEARCH",urlQuery);
            IRequest request=HttpUtil.getInstance()
                    .post(HdojApi.SEARCH_PROBLEM_BYTITLE)
                    .addHeader("Content-Type","application/x-www-form-urlencoded")
                    .addParameter("searchmode","title");
            ((PostRequest)request).addEncoded("content",encode(query));
            Response response= request
                    .execute();
            String html=  new String(response.body().bytes(),"gb2312");
            return html;
        }catch (IOException e){
            e.printStackTrace();
        }
        return "";
    }



    String getPage(String query){
        try{
            IRequest request=HttpUtil.getInstance()
                    .get(HdojApi.PROBLEM_LIST+query);
            Response response= request
                    .execute();
            Logger.d(HdojApi.PROBLEM_LIST+query);
            String html=  new String(response.body().bytes(),"gb2312");
            return html;
        }catch (IOException e){
            e.printStackTrace();
        }
        return "";
    }


    private String getHtml(String query){
        if(query.contains("?vol=")){
            return getPage(query);
        }else{
            return postPage(query);
        }
    }

    private String encode(String s){
        try{
            Logger.d(s+"-----");
            String encoded= URLEncoder.encode(s,"gb2312");
            Logger.d(encoded+"------");
            return encoded;
        }catch (UnsupportedEncodingException e){
            e.printStackTrace();
        }
        return s;

    }
}
