package com.davengo.ga;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.TextView;

import com.davengo.ga.common.configuration.Insole.HandSide;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by malkhameri on 04.08.2015.
 */
public class DeviceListAdapter extends BaseAdapter {
    private LayoutInflater mInflater;
    private List<BluetoothDevice> deviceList;
    private OnPairButtonClickListener mListener;
    private AlertDialog selectDialog;
    private Context context;
    private ViewHolder holder;
    private ProgressDialog mProgressDlg;
    private List<String> serialPortDevicesList = new ArrayList<String>();

    public DeviceListAdapter(Context context) {
        mInflater = LayoutInflater.from(context);
        this.context = context;
    }

    public void setData(List<BluetoothDevice> data) {
        deviceList = data;
    }

    public void setListener(OnPairButtonClickListener listener) {
        mListener = listener;
    }

    public int getCount() {
        return (deviceList == null) ? 0 : deviceList.size();
    }

    public Object getItem(int position) {
        return null;
    }

    public long getItemId(int position) {
        return position;
    }

    public View getView(final int position, View convertView, ViewGroup parent) {


        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.list_item_device, null);

            holder = new ViewHolder();

            holder.nameTv = (TextView) convertView.findViewById(R.id.tv_name);
            holder.addressTv = (TextView) convertView.findViewById(R.id.tv_address);
            //holder.leftChBox = (CheckBox) convertView.findViewById(R.id.leftCheckBox);
            //holder.rightChBox = (CheckBox) convertView.findViewById(R.id.rightCheckBox);
            //holder.pairBtn		= (Button) convertView.findViewById(R.id.btn_pair);
            //holder.dataBtn 		= (Button) convertView.findViewById(R.id.btn_data);

            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        final BluetoothDevice device = deviceList.get(position);

        holder.nameTv.setText(device.getName());
        holder.addressTv.setText(device.getAddress());

/*        holder.rightChBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                //boolean chBoxValue = holder.leftChBox.isChecked() ? false : false;
                //holder.leftChBox.setChecked(false);
                Log.i("left isPressed before ", holder.leftChBox.isPressed() + "");
                holder.leftChBox.setPressed(holder.leftChBox.isPressed() ? false : false);
                Log.i("left isPressed after ", holder.leftChBox.isPressed() + "");
            }
        });
        holder.leftChBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                // holder.rightChBox.isChecked() ? false : false
                //holder.rightChBox.setChecked(false);
                holder.rightChBox.setChecked(holder.rightChBox.isChecked() ? false : false);
            }
        });*/
        convertView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i("Clicked Position >>> ", position + "");



                LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                View dialogLayout = inflater.inflate(R.layout.popup_view, null);
                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setCancelable(true);
                builder.setView(dialogLayout);
                selectDialog = builder.create();
                selectDialog.show();

                Button rightSoleBtn = (Button) dialogLayout.findViewById(R.id.rightSoleButton);
                Button leftSoleBtn = (Button) dialogLayout.findViewById(R.id.leftSoleButton);
                Button pairingBtn = (Button) dialogLayout.findViewById(R.id.pairingButton);
                Button cancelBtn = (Button) dialogLayout.findViewById(R.id.cancelButton);
                pairingBtn.setText((device.getBondState() == BluetoothDevice.BOND_BONDED) ? "Trennen" : "Verbinden");

                rightSoleBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (mListener != null) {
                            mListener.onPairButtonClick(position,HandSide.RIGHT);
                            selectDialog.dismiss();
                        }

                    }
                });
                leftSoleBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mListener.onPairButtonClick(position,HandSide.LEFT);
                        selectDialog.dismiss();

                    }
                });
                pairingBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mListener.onPairButtonClick(position,null);
                        selectDialog.dismiss();

                    }
                });

                cancelBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        selectDialog.dismiss();
                    }
                });


/*                LayoutInflater layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

                View popupView = layoutInflater.inflate(R.layout.popup_view, null);
                final PopupWindow popupWindow = new PopupWindow(
                        popupView,
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT,true);

*//*                Button btnDismiss = (Button) popupView.findViewById(R.id.dismiss);
                btnDismiss.setOnClickListener(new Button.OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        // TODO Auto-generated method stub
                        popupWindow.dismiss();
                    }
                });*//*
                //popupWindow.showAsDropDown(v);
                popupWindow.showAtLocation(popupView, Gravity.CENTER, 0, 0);*/


            }
        });

        // holder.pairBtn.setText((device.getBondState() == BluetoothDevice.BOND_BONDED) ? "Unpair" : "Pair");
        //holder.dataBtn.setVisibility((device.getBondState() == BluetoothDevice.BOND_BONDED) ? View.VISIBLE : View.GONE);

/*        holder.dataBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                for (BluetoothDevice device : deviceList){
                    if(device.getName().contains("Serial Port Device")){
                        serialPortDevicesList.add(device.getAddress());
                    }
                }
                //serialPortDevices = new SerialPortDevices(serialPortDevicesList);
                //serialPortDevices.setSerialPortDevices(serialPortDevicesList);
                //Intent intent = new Intent(context, ViewDataActivity.class);
                //intent.putStringArrayListExtra("SERIAL_PORT_DEVICE", (ArrayList<String>) serialPortDevicesList);
                //intent.putExtra("DEVICE1", serialPortDevicesList.get(0));
                //intent.putExtra("DEVICE2", serialPortDevicesList.get(1));
                //context.startActivity(intent);
            }
        });*/

/*        holder.pairBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mListener != null) {
                    mListener.onPairButtonClick(position);
                }
            }
        });*/

        return convertView;
    }

    static class ViewHolder {
        TextView nameTv;
        TextView addressTv;
    }

    public interface OnPairButtonClickListener {
        public abstract void onPairButtonClick(int position, HandSide side);
    }
}
