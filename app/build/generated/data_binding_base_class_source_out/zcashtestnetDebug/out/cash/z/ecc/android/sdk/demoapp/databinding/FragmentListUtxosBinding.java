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
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import java.lang.NullPointerException;
import java.lang.Override;
import java.lang.String;

public final class FragmentListUtxosBinding implements ViewBinding {
  @NonNull
  private final ConstraintLayout rootView;

  @NonNull
  public final MaterialButton buttonLoad;

  @NonNull
  public final Guideline guidelineContentEnd;

  @NonNull
  public final TextInputEditText inputAddress;

  @NonNull
  public final TextInputEditText inputRangeEnd;

  @NonNull
  public final TextInputEditText inputRangeStart;

  @NonNull
  public final RecyclerView recyclerTransactions;

  @NonNull
  public final TextInputLayout textLayoutAddress;

  @NonNull
  public final TextInputLayout textLayoutRangeEnd;

  @NonNull
  public final TextInputLayout textLayoutRangeStart;

  @NonNull
  public final TextView textStatus;

  private FragmentListUtxosBinding(@NonNull ConstraintLayout rootView,
      @NonNull MaterialButton buttonLoad, @NonNull Guideline guidelineContentEnd,
      @NonNull TextInputEditText inputAddress, @NonNull TextInputEditText inputRangeEnd,
      @NonNull TextInputEditText inputRangeStart, @NonNull RecyclerView recyclerTransactions,
      @NonNull TextInputLayout textLayoutAddress, @NonNull TextInputLayout textLayoutRangeEnd,
      @NonNull TextInputLayout textLayoutRangeStart, @NonNull TextView textStatus) {
    this.rootView = rootView;
    this.buttonLoad = buttonLoad;
    this.guidelineContentEnd = guidelineContentEnd;
    this.inputAddress = inputAddress;
    this.inputRangeEnd = inputRangeEnd;
    this.inputRangeStart = inputRangeStart;
    this.recyclerTransactions = recyclerTransactions;
    this.textLayoutAddress = textLayoutAddress;
    this.textLayoutRangeEnd = textLayoutRangeEnd;
    this.textLayoutRangeStart = textLayoutRangeStart;
    this.textStatus = textStatus;
  }

  @Override
  @NonNull
  public ConstraintLayout getRoot() {
    return rootView;
  }

  @NonNull
  public static FragmentListUtxosBinding inflate(@NonNull LayoutInflater inflater) {
    return inflate(inflater, null, false);
  }

  @NonNull
  public static FragmentListUtxosBinding inflate(@NonNull LayoutInflater inflater,
      @Nullable ViewGroup parent, boolean attachToParent) {
    View root = inflater.inflate(R.layout.fragment_list_utxos, parent, false);
    if (attachToParent) {
      parent.addView(root);
    }
    return bind(root);
  }

  @NonNull
  public static FragmentListUtxosBinding bind(@NonNull View rootView) {
    // The body of this method is generated in a way you would not otherwise write.
    // This is done to optimize the compiled bytecode for size and performance.
    int id;
    missingId: {
      id = R.id.button_load;
      MaterialButton buttonLoad = rootView.findViewById(id);
      if (buttonLoad == null) {
        break missingId;
      }

      id = R.id.guideline_content_end;
      Guideline guidelineContentEnd = rootView.findViewById(id);
      if (guidelineContentEnd == null) {
        break missingId;
      }

      id = R.id.input_address;
      TextInputEditText inputAddress = rootView.findViewById(id);
      if (inputAddress == null) {
        break missingId;
      }

      id = R.id.input_range_end;
      TextInputEditText inputRangeEnd = rootView.findViewById(id);
      if (inputRangeEnd == null) {
        break missingId;
      }

      id = R.id.input_range_start;
      TextInputEditText inputRangeStart = rootView.findViewById(id);
      if (inputRangeStart == null) {
        break missingId;
      }

      id = R.id.recycler_transactions;
      RecyclerView recyclerTransactions = rootView.findViewById(id);
      if (recyclerTransactions == null) {
        break missingId;
      }

      id = R.id.text_layout_address;
      TextInputLayout textLayoutAddress = rootView.findViewById(id);
      if (textLayoutAddress == null) {
        break missingId;
      }

      id = R.id.text_layout_range_end;
      TextInputLayout textLayoutRangeEnd = rootView.findViewById(id);
      if (textLayoutRangeEnd == null) {
        break missingId;
      }

      id = R.id.text_layout_range_start;
      TextInputLayout textLayoutRangeStart = rootView.findViewById(id);
      if (textLayoutRangeStart == null) {
        break missingId;
      }

      id = R.id.text_status;
      TextView textStatus = rootView.findViewById(id);
      if (textStatus == null) {
        break missingId;
      }

      return new FragmentListUtxosBinding((ConstraintLayout) rootView, buttonLoad,
          guidelineContentEnd, inputAddress, inputRangeEnd, inputRangeStart, recyclerTransactions,
          textLayoutAddress, textLayoutRangeEnd, textLayoutRangeStart, textStatus);
    }
    String missingId = rootView.getResources().getResourceName(id);
    throw new NullPointerException("Missing required view with ID: ".concat(missingId));
  }
}
