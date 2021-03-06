// Generated by view binder compiler. Do not edit!
package cash.z.ecc.android.sdk.demoapp.databinding;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.Guideline;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewbinding.ViewBinding;
import cash.z.ecc.android.sdk.demoapp.R;
import com.google.android.material.textfield.TextInputLayout;
import java.lang.NullPointerException;
import java.lang.Override;
import java.lang.String;

public final class FragmentListTransactionsBinding implements ViewBinding {
  @NonNull
  private final ConstraintLayout rootView;

  @NonNull
  public final Guideline guidelineContentEnd;

  @NonNull
  public final RecyclerView recyclerTransactions;

  @NonNull
  public final TextView textInfo;

  @NonNull
  public final TextInputLayout textLayoutEndHeight;

  @NonNull
  public final TextInputLayout textLayoutStartHeight;

  @NonNull
  public final TextView textStatus;

  private FragmentListTransactionsBinding(@NonNull ConstraintLayout rootView,
      @NonNull Guideline guidelineContentEnd, @NonNull RecyclerView recyclerTransactions,
      @NonNull TextView textInfo, @NonNull TextInputLayout textLayoutEndHeight,
      @NonNull TextInputLayout textLayoutStartHeight, @NonNull TextView textStatus) {
    this.rootView = rootView;
    this.guidelineContentEnd = guidelineContentEnd;
    this.recyclerTransactions = recyclerTransactions;
    this.textInfo = textInfo;
    this.textLayoutEndHeight = textLayoutEndHeight;
    this.textLayoutStartHeight = textLayoutStartHeight;
    this.textStatus = textStatus;
  }

  @Override
  @NonNull
  public ConstraintLayout getRoot() {
    return rootView;
  }

  @NonNull
  public static FragmentListTransactionsBinding inflate(@NonNull LayoutInflater inflater) {
    return inflate(inflater, null, false);
  }

  @NonNull
  public static FragmentListTransactionsBinding inflate(@NonNull LayoutInflater inflater,
      @Nullable ViewGroup parent, boolean attachToParent) {
    View root = inflater.inflate(R.layout.fragment_list_transactions, parent, false);
    if (attachToParent) {
      parent.addView(root);
    }
    return bind(root);
  }

  @NonNull
  public static FragmentListTransactionsBinding bind(@NonNull View rootView) {
    // The body of this method is generated in a way you would not otherwise write.
    // This is done to optimize the compiled bytecode for size and performance.
    int id;
    missingId: {
      id = R.id.guideline_content_end;
      Guideline guidelineContentEnd = rootView.findViewById(id);
      if (guidelineContentEnd == null) {
        break missingId;
      }

      id = R.id.recycler_transactions;
      RecyclerView recyclerTransactions = rootView.findViewById(id);
      if (recyclerTransactions == null) {
        break missingId;
      }

      id = R.id.text_info;
      TextView textInfo = rootView.findViewById(id);
      if (textInfo == null) {
        break missingId;
      }

      id = R.id.text_layout_end_height;
      TextInputLayout textLayoutEndHeight = rootView.findViewById(id);
      if (textLayoutEndHeight == null) {
        break missingId;
      }

      id = R.id.text_layout_start_height;
      TextInputLayout textLayoutStartHeight = rootView.findViewById(id);
      if (textLayoutStartHeight == null) {
        break missingId;
      }

      id = R.id.text_status;
      TextView textStatus = rootView.findViewById(id);
      if (textStatus == null) {
        break missingId;
      }

      return new FragmentListTransactionsBinding((ConstraintLayout) rootView, guidelineContentEnd,
          recyclerTransactions, textInfo, textLayoutEndHeight, textLayoutStartHeight, textStatus);
    }
    String missingId = rootView.getResources().getResourceName(id);
    throw new NullPointerException("Missing required view with ID: ".concat(missingId));
  }
}
