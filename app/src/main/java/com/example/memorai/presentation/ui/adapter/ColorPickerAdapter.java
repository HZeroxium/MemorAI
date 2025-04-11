package com.example.memorai.presentation.ui.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.core.content.ContextCompat;
import com.example.memorai.R;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;

public class ColorPickerAdapter extends RecyclerView.Adapter<ColorPickerAdapter.ViewHolder> {

    private Context context;
    private LayoutInflater inflater;
    private List<Integer> colorPickerColors;
    private OnColorPickerClickListener onColorPickerClickListener;

    private int selectedPosition = -1; // Vị trí của màu được chọn, -1 nghĩa là chưa chọn màu nào

    public ColorPickerAdapter(Context context, List<Integer> colorPickerColors) {
        this.context = context;
        this.colorPickerColors = colorPickerColors;
        this.inflater = LayoutInflater.from(context);
    }

    public ColorPickerAdapter(Context context) {
        this(context, getDefaultColors(context));
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = inflater.inflate(R.layout.item_list_color_picker, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        holder.colorPickerView.setBackgroundColor(colorPickerColors.get(position));
        // Hiển thị viền nếu item này được chọn
        holder.selectedBorder.setVisibility(position == selectedPosition ? View.VISIBLE : View.GONE);
    }

    @Override
    public int getItemCount() {
        return colorPickerColors.size();
    }

    // Phương thức để đặt màu được chọn ban đầu
    public void setSelectedColor(int color) {
        int position = colorPickerColors.indexOf(color);
        if (position != -1) {
            int previousPosition = selectedPosition;
            selectedPosition = position;
            if (previousPosition != -1) {
                notifyItemChanged(previousPosition);
            }
            notifyItemChanged(selectedPosition);
        }
    }

    public void setOnColorPickerClickListener(OnColorPickerClickListener listener) {
        this.onColorPickerClickListener = listener;
    }

    public interface OnColorPickerClickListener {
        void onColorPickerClickListener(int colorCode);
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        View colorPickerView;
        View selectedBorder;

        public ViewHolder(View itemView) {
            super(itemView);
            colorPickerView = itemView.findViewById(R.id.color_picker_view);
            selectedBorder = itemView.findViewById(R.id.selected_border);

            itemView.setOnClickListener(this::onClick);
        }

        private void onClick(View v) {
            int position = getAdapterPosition();
            if (position != RecyclerView.NO_POSITION) {
                // Cập nhật vị trí được chọn
                int previousPosition = selectedPosition;
                selectedPosition = position;

                // Làm mới giao diện của item trước đó và item hiện tại
                if (previousPosition != -1) {
                    notifyItemChanged(previousPosition);
                }
                notifyItemChanged(selectedPosition);

                // Gọi listener để thông báo màu được chọn
                if (onColorPickerClickListener != null) {
                    onColorPickerClickListener.onColorPickerClickListener(
                            colorPickerColors.get(position)
                    );
                }
            }
        }
    }

    public static List<Integer> getDefaultColors(Context context) {
        List<Integer> colorPickerColors = new ArrayList<>();
        colorPickerColors.add(ContextCompat.getColor(context, R.color.blue_color_picker));
        colorPickerColors.add(ContextCompat.getColor(context, R.color.brown_color_picker));
        colorPickerColors.add(ContextCompat.getColor(context, R.color.green_color_picker));
        colorPickerColors.add(ContextCompat.getColor(context, R.color.orange_color_picker));
        colorPickerColors.add(ContextCompat.getColor(context, R.color.red_color_picker));
        colorPickerColors.add(ContextCompat.getColor(context, R.color.black));
        colorPickerColors.add(ContextCompat.getColor(context, R.color.red_orange_color_picker));
        colorPickerColors.add(ContextCompat.getColor(context, R.color.sky_blue_color_picker));
        colorPickerColors.add(ContextCompat.getColor(context, R.color.violet_color_picker));
        colorPickerColors.add(ContextCompat.getColor(context, R.color.white));
        colorPickerColors.add(ContextCompat.getColor(context, R.color.yellow_color_picker));
        colorPickerColors.add(ContextCompat.getColor(context, R.color.yellow_green_color_picker));
        return colorPickerColors;
    }
}
